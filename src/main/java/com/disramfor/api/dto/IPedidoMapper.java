package com.disramfor.api.dto;

import com.disramfor.api.entity.DetallePedido;
import com.disramfor.api.entity.Pedido;
import com.disramfor.api.entity.AutoPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IPedidoMapper {

    // --- Mapeo para la respuesta detallada ---
    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nit", target = "clienteNit")
    @Mapping(source = "cliente.nombre", target = "clienteNombre")
    @Mapping(source = "cliente.ciudad", target = "ciudadEntrega")
    @Mapping(source = "cliente.direccion", target = "direccionEntrega")
    @Mapping(source = "cliente.asesor.nombreUsuario", target = "asesorNombre")
    @Mapping(source = "detalles", target = "items", qualifiedByName = "mapDetallesToResponse")
    PedidoResponseDTO toResponse(Pedido pedido);

    // --- MAPEOS NUEVOS PARA LA RESPUESTA RESUMIDA ---
    @Mapping(source = "cliente.nombre", target = "clienteNombre")
    @Mapping(source = "cliente.asesor.username", target = "asesorNombre")
    PedidoResumenDTO toResumen(Pedido pedido);

    // --- Tu método manual para los detalles ---
    @Named("mapDetallesToResponse")
    default List<DetallePedidoResponseDTO> mapDetallesToResponse(List<DetallePedido> detalles) {
        if (detalles == null) {
            return null;
        }
        return detalles.stream().map(d -> {
            DetallePedidoResponseDTO dto = DetallePedidoResponseDTO.builder()
                    .id(d.getId())
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .subtotal(d.getSubtotal())
                    .build();

            AutoPart part = d.getAutoPart();
            if (part != null) {
                dto.setAutoPart(AutoPartDTO.builder()
                        .id(part.getId())
                        .description(part.getDescription())
                        .imageUrl(part.getImageUrl())
                        .price(part.getPrice())
                        .stock(part.getStock())
                        .productType(part.getProductType())
                        .brand(part.getBrand())
                        .model(part.getModel())
                        .engine(part.getEngine())
                        .build());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}