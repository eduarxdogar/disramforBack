package com.disramfor.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedidoResponseDTO {
    private Long id;

    private String productoNombre;    // para mostrar nombre de producto
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    @NotBlank(message = "El código del producto es obligatorio")
    private String productoCodigo;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;

    private String imagenUrl;
    private String pasillo;
    private Integer nivel;
    private Integer espacio;
}
