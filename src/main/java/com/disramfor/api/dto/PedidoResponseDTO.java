package com.disramfor.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponseDTO {
    private Long id;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal descuento;
    private BigDecimal total;

    // Datos del cliente
    private Long clienteId;
    private String clienteNit;
    private String clienteNombre;
    private String direccionEntrega;
    private String ciudadEntrega;

    // Datos del asesor
    private String asesorNombre; // Cambiado de 'asesor' a 'asesorNombre' para claridad

    // Items del pedido
    private List<DetallePedidoResponseDTO> items;
}