package com.disramfor.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PedidoResumenDTO {
    private Long id;
    private LocalDateTime fecha;
    private String clienteNombre;
    private String asesorNombre; // Asumiendo que tienes un asesor en el pedido
    private String estado;
    private BigDecimal total;
}

