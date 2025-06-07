package com.disramfor.api.dto;

import com.disramfor.api.entity.EstadoPedido;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponseDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fecha;
    private String clienteNombre;   // <-- nuevo
    private String clienteNit;
    private EstadoPedido estado;
    private BigDecimal total;
    private List<DetallePedidoResponseDTO> items;
    private String asesor;
    private String ciudadEntrega;
    private String direccionEntrega;
}