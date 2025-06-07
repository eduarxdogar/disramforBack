package com.disramfor.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetallePedidoRequestDTO {
    @NotBlank(message="Código de producto obligatorio")
    private String productoCodigo;

    @NotNull
    @Min(1)
    private Integer cantidad;
}

