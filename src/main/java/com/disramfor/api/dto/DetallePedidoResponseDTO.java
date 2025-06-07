package com.disramfor.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedidoResponseDTO {
    private Long id;
    private String productoCodigo;
    private String productoNombre;    // para mostrar nombre de producto
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
