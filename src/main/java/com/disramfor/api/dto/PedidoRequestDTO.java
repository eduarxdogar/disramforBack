package com.disramfor.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PedidoRequestDTO {
    @NotNull(message="El cliente es obligatorio")
    private Long clienteId;
    // Asumimos que también quieres saber qué asesor hizo el pedido
    @NotNull(message = "El ID del asesor es obligatorio")
    private Long asesorId;

    @NotEmpty(message="Debe haber al menos un ítem")
    private List<DetallePedidoRequestDTO> items;
}
