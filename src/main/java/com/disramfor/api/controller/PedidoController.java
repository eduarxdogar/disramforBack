package com.disramfor.api.controller;

import com.disramfor.api.dto.PedidoRequestDTO;
import com.disramfor.api.dto.PedidoResponseDTO;
import com.disramfor.api.dto.PedidoResumenDTO;
import com.disramfor.api.entity.EstadoPedido;
import com.disramfor.api.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASESOR')")
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CARTERA . 'ASESOR')")
    public ResponseEntity<PedidoResponseDTO> actualizarPedidoCompleto(
            @PathVariable Long id,
            @Valid @RequestBody PedidoRequestDTO pedidoRequestDTO) {
        PedidoResponseDTO pedidoActualizado = service.actualizarPedido(id, pedidoRequestDTO);
        return ResponseEntity.ok(pedidoActualizado);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CARTERA')")
    public ResponseEntity<Void> actualizarEstadoPedido(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        String estadoStr = requestBody.get("estado");
        if (estadoStr == null) {
            return ResponseEntity.badRequest().build();
        }
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr.toUpperCase());
        service.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        service.eliminarPedido(id);
    }
}

