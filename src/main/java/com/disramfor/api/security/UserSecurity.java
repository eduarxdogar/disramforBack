package com.disramfor.api.security;

import com.disramfor.api.entity.Pedido;
import com.disramfor.api.exception.ResourceNotFoundException;
import com.disramfor.api.repository.IPedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final IPedidoRepository pedidoRepository;

    public boolean isOwner(Authentication authentication, Long pedidoId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();

        //  (redundancia útil)
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));

        // 1. Si el usuario es el ASESOR del cliente del pedido
        if (pedido.getCliente() != null && pedido.getCliente().getAsesor() != null) {
            String emailAsesor = pedido.getCliente().getAsesor().getEmail();
            if (emailAsesor.equals(currentUsername)) {
                return true;
            }
        }

        //  Si el usuario es el CLIENTE mismo (si hay login de clientes
        // verificado por email)
        if (pedido.getCliente() != null && pedido.getCliente().getEmail() != null) {
            if (pedido.getCliente().getEmail().equalsIgnoreCase(currentUsername)) {
                return true;
            }
        }

        return false;
    }
}
