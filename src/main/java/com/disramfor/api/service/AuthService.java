package com.disramfor.api.service;

import com.disramfor.api.dto.AuthenticationRequest;
import com.disramfor.api.dto.AuthenticationResponse;
import com.disramfor.api.dto.RegistroRequest;
import com.disramfor.api.entity.Usuario;
import com.disramfor.api.repository.IUsuarioRepository;
import com.disramfor.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegistroRequest request) {
        try {
            Optional<Usuario> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                throw new IllegalStateException("El email ya está registrado.");
            }

            var user = Usuario.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .rol(request.getRol())
                    .build();
            userRepository.save(user);
            var jwtToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder().token(jwtToken).build();
        } catch (DataIntegrityViolationException e) {
            // Este catch es para cuando la base de datos detecta un email duplicado
            throw new IllegalStateException("Error al registrar: el email ya está en uso.", e);
        }
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        // Usa el AuthenticationManager para validar las credenciales
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // Si las credenciales son válidas, busca el usuario en la base de datos
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        // Genera el token JWT
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}
