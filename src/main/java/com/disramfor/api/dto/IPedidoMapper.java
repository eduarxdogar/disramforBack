package com.disramfor.api.dto;

import com.disramfor.api.entity.DetallePedido;
import com.disramfor.api.entity.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel="spring")
public interface IPedidoMapper {

    @Mapping(source="cliente.id", target="clienteId")
    @Mapping(target="clienteNit",    source="cliente.nit")
    @Mapping(target="clienteNombre", source="cliente.nombre")

    @Mapping(source="cliente.ciudad",    target="ciudadEntrega")
    @Mapping(source="cliente.direccion", target="direccionEntrega")
    @Mapping(source="cliente.asesor",       target="asesor")
    @Mapping(source="detalles", target="items", qualifiedByName="mapDetallesToResponse")

    PedidoResponseDTO toResponse(Pedido pedido);

    @Named("mapDetallesToResponse")
    default List<DetallePedidoResponseDTO> mapDetallesToResponse(List<DetallePedido> detalles) {
        return detalles.stream().map(d -> {
            DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
            dto.setId(d.getId());
            dto.setProductoCodigo(d.getProducto().getCodigo());
            dto.setProductoNombre(d.getProducto().getNombre());
            dto.setCantidad(d.getCantidad());
            dto.setPrecioUnitario(d.getPrecioUnitario());
            dto.setSubtotal(d.getSubtotal());
            return dto;
        }).toList();
    }
}


