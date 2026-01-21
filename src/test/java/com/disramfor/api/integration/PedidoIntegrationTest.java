package com.disramfor.api.integration;

import com.disramfor.api.dto.*;
import com.disramfor.api.entity.*;
import com.disramfor.api.repository.*;
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

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@AutoConfigureWebMvc
@Transactional
class PedidoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPedidoRepository pedidoRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("resource")
@Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("disramfor_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    private Cliente clientePrueba;
    private Producto producto1, producto2;
    private PedidoRequestDTO pedidoRequestDTO;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        clienteRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Crear categoría
        Categoria categoria = new Categoria();
        categoria.setNombre("Test Category");
        categoria = categoriaRepository.save(categoria);

        // Crear cliente
        clientePrueba = new Cliente();
        clientePrueba.setNit("123456789");
        clientePrueba.setNombre("Cliente Test");
        clientePrueba = clienteRepository.save(clientePrueba);

        // Crear productos
        producto1 = new Producto();
        producto1.setCodigo("PROD001");
        producto1.setNombre("Producto 1");
        producto1.setPrecioUnitario(new BigDecimal("100.00"));
        producto1.setCategoria(categoria);
        producto1 = productoRepository.save(producto1);

        producto2 = new Producto();
        producto2.setCodigo("PROD002");
        producto2.setNombre("Producto 2");
        producto2.setPrecioUnitario(new BigDecimal("50.00"));
        producto2.setCategoria(categoria);
        producto2 = productoRepository.save(producto2);

        // Crear request DTO
        DetallePedidoRequestDTO detalle1 = new DetallePedidoRequestDTO();
        detalle1.setProductoCodigo("PROD001");
        detalle1.setCantidad(2);

        DetallePedidoRequestDTO detalle2 = new DetallePedidoRequestDTO();
        detalle2.setProductoCodigo("PROD002");
        detalle2.setCantidad(3);

        pedidoRequestDTO = new PedidoRequestDTO();
        pedidoRequestDTO.setClienteId(clientePrueba.getId());
        pedidoRequestDTO.setAsesorId(1L);
        pedidoRequestDTO.setItems(Arrays.asList(detalle1, detalle2));
    }

    // =====================================================================
    // TEST 1: Flujo completo de gestión de pedidos
    // =====================================================================
    @Test
    @DisplayName("Debe gestionar el ciclo completo de un pedido")
    @WithMockUser(roles = "ADMIN")
    void debeGestionarCicloCompletoPedido() throws Exception {
        // 1. CREAR pedido
        String pedidoJson = objectMapper.writeValueAsString(pedidoRequestDTO);

        String responseBody = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clientePrueba.getId()))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(350.00)) // (100*2) + (50*3) = 350
                .andReturn()
                .getResponse()
                .getContentAsString();

        PedidoResponseDTO pedidoCreado = objectMapper.readValue(responseBody, PedidoResponseDTO.class);
        Long pedidoId = pedidoCreado.getId();

        // 2. LEER pedido completo
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].productoCodigo").value("PROD001"))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[1].productoCodigo").value("PROD002"))
                .andExpect(jsonPath("$.items[1].cantidad").value(3));

        // 3. LISTAR pedidos (debe aparecer el creado)
        mockMvc.perform(get("/api/pedidos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(pedidoId))
                .andExpect(jsonPath("$.content[0].clienteNombre").value("Cliente Test"));

        // 4. ACTUALIZAR estado del pedido
        String cambioEstado = "{\"estado\": \"ENVIADO\"}";

        mockMvc.perform(patch("/api/pedidos/{id}", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambioEstado))
                .andExpect(status().isNoContent());

        // Verificar que el estado cambió
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADO"));

        // 5. ELIMINAR pedido
        mockMvc.perform(delete("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNoContent());

        // 6. VERIFICAR que fue eliminado
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // TEST 2: Cálculo correcto de totales
    // =====================================================================
    @Test
    @DisplayName("Debe calcular correctamente los totales del pedido")
    @WithMockUser(roles = "ADMIN")
    void debeCalcularCorrectamenteTotales() throws Exception {
        String pedidoJson = objectMapper.writeValueAsString(pedidoRequestDTO);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(350.00))
                .andExpect(jsonPath("$.items[0].precioUnitario").value(100.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(200.00))
                .andExpect(jsonPath("$.items[1].precioUnitario").value(50.00))
                .andExpect(jsonPath("$.items[1].subtotal").value(150.00));
    }

    // =====================================================================
    // TEST 3: Validaciones de negocio
    // =====================================================================
    @Test
    @DisplayName("Debe validar reglas de negocio correctamente")
    @WithMockUser(roles = "ADMIN")
    void debeValidarReglasNegocio() throws Exception {
        // 1. Error: Cliente inexistente
        PedidoRequestDTO pedidoClienteInexistente = new PedidoRequestDTO();
        pedidoClienteInexistente.setClienteId(999L);
        pedidoClienteInexistente.setAsesorId(1L);
        pedidoClienteInexistente.setItems(pedidoRequestDTO.getItems());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoClienteInexistente)))
                .andExpect(status().isNotFound());

        // 2. Error: Producto inexistente
        DetallePedidoRequestDTO detalleProductoInexistente = new DetallePedidoRequestDTO();
        detalleProductoInexistente.setProductoCodigo("PROD999");
        detalleProductoInexistente.setCantidad(1);

        PedidoRequestDTO pedidoProductoInexistente = new PedidoRequestDTO();
        pedidoProductoInexistente.setClienteId(clientePrueba.getId());
        pedidoProductoInexistente.setAsesorId(1L);
        pedidoProductoInexistente.setItems(Arrays.asList(detalleProductoInexistente));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoProductoInexistente)))
                .andExpect(status().isNotFound());

        // 3. Error: Pedido sin items
        PedidoRequestDTO pedidoSinItems = new PedidoRequestDTO();
        pedidoSinItems.setClienteId(clientePrueba.getId());
        pedidoSinItems.setAsesorId(1L);
        pedidoSinItems.setItems(Arrays.asList()); // Lista vacía

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoSinItems)))
                .andExpect(status().isBadRequest());
    }
}
