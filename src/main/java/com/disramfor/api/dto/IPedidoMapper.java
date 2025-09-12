package com.disramfor.api.dto;

import com.disramfor.api.entity.DetallePedido;
import com.disramfor.api.entity.Pedido;
import com.disramfor.api.entity.Producto;
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
    @Mapping(source = "cliente.asesor.nombreUsuario", target = "asesorNombre") // Asume que el campo es 'username'
    @Mapping(source = "detalles", target = "items", qualifiedByName = "mapDetallesToResponse")
    PedidoResponseDTO toResponse(Pedido pedido);

    // --- MAPEOS NUEVOS PARA LA RESPUESTA RESUMIDA ---
    @Mapping(source = "cliente.nombre", target = "clienteNombre")
    @Mapping(source = "cliente.asesor.username", target = "asesorNombre") // Asume que el campo es 'username'
    PedidoResumenDTO toResumen(Pedido pedido);


    // --- Tu método manual para los detalles (está bien como está) ---
    @Named("mapDetallesToResponse")
    default List<DetallePedidoResponseDTO> mapDetallesToResponse(List<DetallePedido> detalles) {
        if (detalles == null) {
            return null;
        }
        return detalles.stream().map(d -> {
            DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
            dto.setId(d.getId());
            dto.setCantidad(d.getCantidad());
            dto.setPrecioUnitario(d.getPrecioUnitario());
            dto.setSubtotal(d.getSubtotal());

            Producto producto = d.getProducto();
            if (producto != null) {
                dto.setProductoCodigo(producto.getCodigo());
                dto.setProductoNombre(producto.getNombre());
                dto.setImagenUrl(producto.getImagenUrl());
                dto.setPasillo(producto.getPasillo());
                dto.setNivel(producto.getNivel());
                dto.setEspacio(producto.getEspacio());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}