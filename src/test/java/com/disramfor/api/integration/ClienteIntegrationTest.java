package com.disramfor.api.integration;

import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.repository.IClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración completo para Cliente
 *
 * @SpringBootTest - Carga el contexto completo de la aplicación
 * @Testcontainers - Habilita el uso de TestContainers
 * @ActiveProfiles - Usa el perfil de integration-test
 * @Transactional - Cada test se ejecuta en una transacción que se rollback
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@AutoConfigureWebMvc
@Transactional
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // TestContainer con MySQL real
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("disramfor_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reutiliza el contenedor entre tests

    private ClienteRequestDTO clienteRequestDTO;

    @BeforeEach
    void setUp() {
        // Limpiar datos entre tests
        clienteRepository.deleteAll();

        // Preparar datos de prueba
        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNit("123456789");
        clienteRequestDTO.setNombre("Empresa Test S.A.S");
        clienteRequestDTO.setDireccion("Calle 123 #45-67");
        clienteRequestDTO.setCiudad("Bogotá");
        clienteRequestDTO.setTelefono("3001234567");
        clienteRequestDTO.setEmail("test@empresa.com");
    }

    // =====================================================================
    // TEST 1: Flujo completo de CRUD de clientes
    // =====================================================================
    @Test
    @DisplayName("Debe realizar CRUD completo de cliente")
    @WithMockUser(roles = "ADMIN") // Simula usuario autenticado
    void debeRealizarCrudCompletoCliente() throws Exception {
        // 1. CREAR cliente
        String clienteJson = objectMapper.writeValueAsString(clienteRequestDTO);

        String responseBody = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nit").value("123456789"))
                .andExpect(jsonPath("$.nombre").value("Empresa Test S.A.S"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ClienteResponseDTO clienteCreado = objectMapper.readValue(responseBody, ClienteResponseDTO.class);
        Long clienteId = clienteCreado.getId();

        // 2. LEER cliente por ID
        mockMvc.perform(get("/api/clientes/{id}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.nit").value("123456789"));

        // 3. ACTUALIZAR cliente
        clienteRequestDTO.setNombre("Empresa Actualizada S.A.S");
        clienteRequestDTO.setCiudad("Medellín");

        String clienteActualizadoJson = objectMapper.writeValueAsString(clienteRequestDTO);

        mockMvc.perform(put("/api/clientes/{id}", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteActualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empresa Actualizada S.A.S"))
                .andExpect(jsonPath("$.ciudad").value("Medellín"));

        // 4. BUSCAR por NIT
        mockMvc.perform(get("/api/clientes/nit/{nit}", "123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empresa Actualizada S.A.S"));

        // 5. LISTAR clientes (debe aparecer el creado)
        mockMvc.perform(get("/api/clientes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 6. ELIMINAR cliente
        mockMvc.perform(delete("/api/clientes/{id}", clienteId))
                .andExpect(status().isNoContent());

        // 7. VERIFICAR que fue eliminado
        mockMvc.perform(get("/api/clientes/{id}", clienteId))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // TEST 2: Búsqueda y filtrado
    // =====================================================================
    @Test
    @DisplayName("Debe realizar búsquedas y filtros correctamente")
    @WithMockUser(roles = "ADMIN")
    void debeRealizarBusquedasYFiltros() throws Exception {
        // Crear múltiples clientes de prueba
        Cliente cliente1 = new Cliente();
        cliente1.setNit("111111111");
        cliente1.setNombre("ABC Empresa");
        cliente1.setCiudad("Bogotá");
        clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNit("222222222");
        cliente2.setNombre("XYZ Compañía");
        cliente2.setCiudad("Medellín");
        clienteRepository.save(cliente2);

        Cliente cliente3 = new Cliente();
        cliente3.setNit("333333333");
        cliente3.setNombre("ABC Corporation");
        cliente3.setCiudad("Cali");
        clienteRepository.save(cliente3);

        // Búsqueda por término "ABC" (debe encontrar 2)
        mockMvc.perform(get("/api/clientes")
                        .param("term", "ABC")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].nombre",
                        everyItem(containsString("ABC"))));

        // Búsqueda por NIT parcial
        mockMvc.perform(get("/api/clientes/search")
                        .param("term", "111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nit").value("111111111"));

        // Búsqueda exacta por NIT
        mockMvc.perform(get("/api/clientes/nit/222222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("XYZ Compañía"));
    }

    // =====================================================================
    // TEST 3: Validaciones y errores
    // =====================================================================
    @Test
    @DisplayName("Debe manejar validaciones y errores correctamente")
    @WithMockUser(roles = "ADMIN")
    void debeManejarValidacionesYErrores() throws Exception {
        // 1. Error: Cliente sin NIT (campo obligatorio)
        ClienteRequestDTO clienteSinNit = new ClienteRequestDTO();
        clienteSinNit.setNombre("Empresa Sin NIT");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteSinNit)))
                .andExpect(status().isBadRequest());

        // 2. Error: Email inválido
        ClienteRequestDTO clienteEmailInvalido = new ClienteRequestDTO();
        clienteEmailInvalido.setNit("123456789");
        clienteEmailInvalido.setNombre("Empresa Test");
        clienteEmailInvalido.setEmail("email-invalido"); // Email sin formato correcto

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteEmailInvalido)))
                .andExpect(status().isBadRequest());

        // 3. Error: Cliente no encontrado
        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound());

        // 4. Error: NIT duplicado
        // Primero crear un cliente
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isOk());

        // Intentar crear otro con el mismo NIT
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isConflict()); // Debe fallar por NIT duplicado
    }
}