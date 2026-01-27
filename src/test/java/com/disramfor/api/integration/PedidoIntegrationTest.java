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
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Testcontainers -- Eliminado porque no hay Docker configurado actualmente
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
        private AutoPartRepository autoPartRepository;

        @Autowired
        private ICategoriaRepository categoriaRepository;

        @Autowired
        private ObjectMapper objectMapper;

        // MySQLContainer eliminado temporalmente para evitar problemas de entorno

        private Cliente clientePrueba;
        private AutoPart producto1, producto2;
        private PedidoRequestDTO pedidoRequestDTO;

        @BeforeEach
        void setUp() {
                // Limpiar datos
                pedidoRepository.deleteAll();
                autoPartRepository.deleteAll();
                clienteRepository.deleteAll();
                categoriaRepository.deleteAll(); // Aunque no se use para productos, limpiar por si acaso

                // Crear cliente
                clientePrueba = new Cliente();
                clientePrueba.setNit("123456789");
                clientePrueba.setNombre("Cliente Test");
                clientePrueba = clienteRepository.save(clientePrueba);

                // Crear productos (AutoParts)
                producto1 = new AutoPart();
                producto1.setId("PROD001");
                producto1.setDescription("Producto 1");
                producto1.setPrice(new BigDecimal("100.00"));
                producto1.setStock(100);
                producto1.setProductType("TYPE_A");
                producto1 = autoPartRepository.save(producto1);

                producto2 = new AutoPart();
                producto2.setId("PROD002");
                producto2.setDescription("Producto 2");
                producto2.setPrice(new BigDecimal("50.00"));
                producto2.setStock(50);
                producto2.setProductType("TYPE_A");
                producto2 = autoPartRepository.save(producto2);

                // Crear request DTO
                DetallePedidoRequestDTO detalle1 = new DetallePedidoRequestDTO();
                detalle1.setAutoPartId("PROD001");
                detalle1.setCantidad(2);

                DetallePedidoRequestDTO detalle2 = new DetallePedidoRequestDTO();
                detalle2.setAutoPartId("PROD002");
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
                                // Cambio: productoCodigo ya no está en la raíz del item, sino dentro de
                                // autoPart
                                .andExpect(jsonPath("$.items[0].autoPart.id").value("PROD001"))
                                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                                .andExpect(jsonPath("$.items[1].autoPart.id").value("PROD002"))
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
                detalleProductoInexistente.setAutoPartId("PROD999");
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
                pedidoSinItems.setItems(Collections.emptyList()); // Lista vacía

                mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(pedidoSinItems)))
                                .andExpect(status().isBadRequest());
        }
}
