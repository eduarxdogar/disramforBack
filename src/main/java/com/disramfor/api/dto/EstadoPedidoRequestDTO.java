package com.disramfor.api.dto;

import com.disramfor.api.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoPedidoRequestDTO {
    @NotNull
    private EstadoPedido estado;
}
