package com.disramfor.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetallePedidoRequestDTO {
    @NotNull(message = "Código de AutoPart obligatorio")
    @JsonProperty("id")
    @JsonAlias({ "autoPartId", "productId", "codigo", "productoCodigo" })
    private String autoPartId; // Changed to String

    @NotNull
    @Min(1)
    private Integer cantidad;
}
