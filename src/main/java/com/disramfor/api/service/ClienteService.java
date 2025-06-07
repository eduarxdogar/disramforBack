package com.disramfor.api.service;

import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.IClienteMapper;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.repository.IClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private IClienteRepository repo;

    @Autowired
    private IClienteMapper mapper;

    public List<ClienteResponseDTO> listar() {
        List<Cliente> todos = repo.findAll();
        List<ClienteResponseDTO> salida = new ArrayList<>();

        for (Cliente c : todos) {
            // descartamos cualquier fila inesperada
            if (c != null && c.getId() != null) {
                // usamos el constructor de tu DTO en vez de MapStruct
                salida.add(new ClienteResponseDTO(c));
            }
        }

        return salida;
    }
    // Busca exacto por NIT
    public ClienteResponseDTO buscarPorNit(String nit) {
        Cliente c = repo.findByNit(nit)
                .orElseThrow(() -> new RuntimeException("No existe cliente con NIT: " + nit));
        return mapper.toResponse(c);
    }
    // Búsqueda parcial por NIT o nombre, conversión manual para evitar NPE
    public List<ClienteResponseDTO> buscarPorTermino(String term) {
        List<Cliente> encontrados = repo.searchByNitOrNombre(term);
        List<ClienteResponseDTO> salida = new ArrayList<>();

        for (Cliente c : encontrados) {
            // 1) descartamos cualquier entrada inesperada
            if (c != null && c.getId() != null) {
                // 2) creamos el DTO con tu constructor
                salida.add(new ClienteResponseDTO(c));
            }
        }

        return salida;
    }



    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente c = buscarEntidadPorId(id);
        return mapper.toResponse(c);
    }

    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        // 1) Convierte el DTO a entidad
        Cliente entidad = mapper.toEntity(dto);
        // 2) Guarda en BD
        Cliente saved = repo.save(entidad);
        // 3) Mapea manualmente al DTO de respuesta
        return new ClienteResponseDTO(saved);
    }

    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        // 1) obtenemos la entidad existente (o lanzamos si no existe)
        Cliente existing = buscarEntidadPorId(id);
        // 2) copiamos los campos no nulos del DTO sobre la entidad
        mapper.updateEntityFromDto(dto, existing);
        // 3) guardamos
        Cliente saved = repo.save(existing);
        // 4) convertimos manualmente al DTO, evitando MapStruct
        return new ClienteResponseDTO(saved);
    }

    public void eliminar(Long id) {
        repo.delete(buscarEntidadPorId(id));
    }

    private Cliente buscarEntidadPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe cliente con id: " + id));
    }
}
