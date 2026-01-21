package com.disramfor.api.integration;

import com.disramfor.api.dto.AuthenticationRequest;
import com.disramfor.api.dto.RegistroRequest;
import com.disramfor.api.entity.Rol;
import com.disramfor.api.entity.Usuario;
import com.disramfor.api.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@AutoConfigureWebMvc
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("resource")
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("disramfor_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        // Crear usuario de prueba
        Usuario usuario = Usuario.builder()
                .nombreUsuario("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .rol(Rol.ADMIN)
                .build();
        usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("Debe autenticar usuario con credenciales válidas")
    void debeAutenticarUsuarioValido() throws Exception {
        AuthenticationRequest loginRequest = new AuthenticationRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Debe rechazar credenciales inválidas")
    void debeRechazarCredencialesInvalidas() throws Exception {
        AuthenticationRequest loginRequest = new AuthenticationRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("passwordIncorrecto");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debe registrar nuevo usuario")
    void debeRegistrarNuevoUsuario() throws Exception {
        RegistroRequest registroRequest = new RegistroRequest();
        registroRequest.setNombreUsuario("nombre");
        registroRequest.setEmail("nuevo@example.com");
        registroRequest.setPassword("password123");
        registroRequest.setRol(Rol.SUPERVISOR);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Debe rechazar registro con email duplicado")
    void debeRechazarEmailDuplicado() throws Exception {
        RegistroRequest registroRequest = new RegistroRequest();
        registroRequest.setNombreUsuario("otrouser");
        registroRequest.setEmail("test@example.com"); // Email ya existe
        registroRequest.setPassword("password123");
        registroRequest.setRol(Rol.SUPERVISOR);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isConflict());
    }
}
