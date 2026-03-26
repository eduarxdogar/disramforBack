package com.disramfor.api.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DetallePedidoResponseDTO {
    private Long id;
    private Long pedidoId;
    private AutoPartDTO autoPart;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}