
package com.disramfor.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedidoResponseDTO {
    private Long id;
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Datos del producto
    @NotBlank(message = "El código del producto es obligatorio")
    private String productoCodigo;
    private String productoNombre;
    private String imagenUrl;
    private String pasillo;
    private Integer nivel;
    private Integer espacio;
}