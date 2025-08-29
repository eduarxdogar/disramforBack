package com.disramfor.api.controller;


import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listarClientes(
            @RequestParam(required = false, defaultValue = "") String term,
            Pageable pageable) {

        return ResponseEntity.ok(service.listar(pageable, term));
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO get(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ClienteResponseDTO crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return service.crear(dto);
    }

    @PutMapping("/{id}")
    public ClienteResponseDTO actualizar(@PathVariable Long id,
                                         @Valid @RequestBody ClienteRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    // 1) Búsqueda exacta por NIT
    @GetMapping("/nit/{nit}")
    public ClienteResponseDTO getByNit(@PathVariable String nit) {
        return service.buscarPorNit(nit);
    }

    // 2) Búsqueda parcial por término (nit o nombre)
    @GetMapping("/search")
    public List<ClienteResponseDTO> search(@RequestParam("term") String term) {
        return service.buscarPorTermino(term);
    }
}

