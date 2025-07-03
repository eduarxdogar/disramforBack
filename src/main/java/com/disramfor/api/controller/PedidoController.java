package com.disramfor.api.controller;

import com.disramfor.api.dto.PedidoRequestDTO;
import com.disramfor.api.dto.PedidoResponseDTO;
import com.disramfor.api.dto.PedidoResumenDTO;
import com.disramfor.api.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return service.crearPedido(dto);
    }


    @GetMapping
    public Page<PedidoResumenDTO> listar(Pageable pageable) {
        return service.listarPedidosResumen(pageable);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO get(@PathVariable Long id) {
        return service.obtenerPedido(id);
    }

    // ————— Actualización parcial (incluye estado) —————
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchPedido(
            @PathVariable Long id,
            @RequestBody Map<String,Object> cambios
    ) {
        service.actualizarParcial(id, cambios);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminarPedido(id);
    }




}
