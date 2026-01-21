package com.disramfor.api.service;

import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.dto.IClienteMapper;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.entity.Rol;
import com.disramfor.api.entity.Usuario;
import com.disramfor.api.exception.ResourceNotFoundException;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final IClienteRepository repo;
    private final IClienteMapper mapper;
    private final IUsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(Pageable pageable, String term) {
        Usuario usuarioLogueado = getAuthenticatedUser();
        String searchTerm = (term == null) ? "" : term;

        if (usuarioLogueado.getRol() == Rol.ADMIN) {
            return repo.searchByTerm(searchTerm, pageable).map(mapper::toResponse);
        } else if (usuarioLogueado.getRol() == Rol.ASESOR) {
            return repo.searchByAsesorIdAndTerm(usuarioLogueado.getId(), searchTerm, pageable).map(mapper::toResponse);
        }
        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        Usuario usuarioLogueado = getAuthenticatedUser();
        if (usuarioLogueado.getRol() == Rol.ASESOR && !cliente.getAsesor().getId().equals(usuarioLogueado.getId())) {
            throw new AccessDeniedException("No tienes permiso para ver este cliente.");
        }

        return mapper.toResponse(cliente);
    }

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        Usuario asesor = usuarioRepository.findById(dto.getAsesorId())
                .orElseThrow(() -> new ResourceNotFoundException("Asesor no encontrado con ID: " + dto.getAsesorId()));
        Cliente cliente = mapper.toEntity(dto);
        cliente.setAsesor(asesor);
        return mapper.toResponse(repo.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        Usuario asesor = usuarioRepository.findById(dto.getAsesorId())
                .orElseThrow(() -> new ResourceNotFoundException("Asesor no encontrado con ID: " + dto.getAsesorId()));

        mapper.updateEntityFromDto(dto, cliente);
        cliente.setAsesor(asesor);
        return mapper.toResponse(repo.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO crearParaAsesor(ClienteRequestDTO dto) {
        Usuario asesorLogueado = getAuthenticatedUser();
        Cliente cliente = mapper.toEntity(dto);
        cliente.setAsesor(asesorLogueado);
        return mapper.toResponse(repo.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO actualizarParaAsesor(Long id, ClienteRequestDTO dto) {
        Usuario asesorLogueado = getAuthenticatedUser();
        Cliente cliente = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (!cliente.getAsesor().getId().equals(asesorLogueado.getId())) {
            throw new AccessDeniedException("No tienes permiso para actualizar este cliente.");
        }

        mapper.updateEntityFromDto(dto, cliente);
        return mapper.toResponse(repo.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuarioLogueado = getAuthenticatedUser();
        Cliente cliente = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (usuarioLogueado.getRol() == Rol.ASESOR && !cliente.getAsesor().getId().equals(usuarioLogueado.getId())) {
            throw new AccessDeniedException("No tienes permiso para eliminar este cliente.");
        }

        repo.delete(cliente);
    }

    public ClienteResponseDTO buscarPorNit(String nit) {
        Cliente c = repo.findByNit(nit)
                .orElseThrow(() -> new ResourceNotFoundException("No existe cliente con NIT: " + nit));
        return mapper.toResponse(c);
    }

    public List<ClienteResponseDTO> buscarPorTermino(String term) {
        return repo.searchByTerm(term, Pageable.unpaged())
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Acceso denegado. Se requiere autenticación.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        String username = principal.toString();
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario '" + username + "' no encontrado."));
    }
}
