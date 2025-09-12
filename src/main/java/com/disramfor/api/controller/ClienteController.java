package com.disramfor.api.controller;

import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.dto.IValidationGroups;
import com.disramfor.api.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

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
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ClienteResponseDTO crear(@Validated(IValidationGroups.AdminAction.class) @RequestBody ClienteRequestDTO dto) {
        return service.crear(dto);
    }

    @PostMapping("/asesor")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ASESOR')")
    public ClienteResponseDTO crearClienteComoAsesor(@Validated(IValidationGroups.AsesorAction.class) @RequestBody ClienteRequestDTO dto) {
        return service.crearParaAsesor(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ClienteResponseDTO actualizar(@PathVariable Long id, @Validated(IValidationGroups.AdminAction.class) @RequestBody ClienteRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    // --- ¡NUEVO ENDPOINT DE ACTUALIZAR PARA ASESOR! ---
    @PutMapping("/asesor/{id}")
    @PreAuthorize("hasAuthority('ASESOR')")
    public ClienteResponseDTO actualizarComoAsesor(@PathVariable Long id, @Validated(IValidationGroups.AsesorAction.class) @RequestBody ClienteRequestDTO dto) {
        return service.actualizarParaAsesor(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ASESOR')") // Permitimos a ambos roles
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id); // El servicio ya contiene la lógica de seguridad
    }

    @GetMapping("/nit/{nit}")
    public ClienteResponseDTO getByNit(@PathVariable String nit) {
        return service.buscarPorNit(nit);
    }

    @GetMapping("/search")
    public List<ClienteResponseDTO> search(@RequestParam("term") String term) {
        return service.buscarPorTermino(term);
    }
}
