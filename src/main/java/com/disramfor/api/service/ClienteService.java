package com.disramfor.api.service;

import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.IClienteMapper;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.repository.IClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private IClienteRepository repo;

    @Autowired
    private IClienteMapper mapper;

    public List<ClienteResponseDTO> listar() {


        return repo.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    // Busca exacto por NIT
    public ClienteResponseDTO buscarPorNit(String nit) {
        Cliente c = repo.findByNit(nit)
                .orElseThrow(() -> new RuntimeException("No existe cliente con NIT: " + nit));
        return mapper.toResponse(c);
    }
    // Búsqueda parcial por NIT o nombre, conversión manual para evitar NPE
    public List<ClienteResponseDTO> buscarPorTermino(String term) {
          return repo.searchByNitOrNombre(term)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }



    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente c = buscarEntidadPorId(id);
        return mapper.toResponse(c);
    }

    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        Cliente entidad = mapper.toEntity(dto);
        Cliente saved = repo.save(entidad);
        return mapper.toResponse(saved);
    }

    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente existing = buscarEntidadPorId(id);
        mapper.updateEntityFromDto(dto, existing);
        Cliente saved = repo.save(existing);
        // ¡Ahora usamos el mapper también aquí!
        return mapper.toResponse(saved);
    }

    public void eliminar(Long id) {
        repo.delete(buscarEntidadPorId(id));
    }

    private Cliente buscarEntidadPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe cliente con id: " + id));
    }
}
