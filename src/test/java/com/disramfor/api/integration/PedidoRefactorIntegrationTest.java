package com.disramfor.api.integration;

import com.disramfor.api.dto.DetallePedidoRequestDTO;
import com.disramfor.api.dto.PedidoRequestDTO;
import com.disramfor.api.dto.PedidoResponseDTO;
import com.disramfor.api.entity.AutoPart;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.repository.AutoPartRepository;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IPedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Testcontainers -- Eliminado porque no hay Docker
@ActiveProfiles("test") // Usamos perfil test que suele ser H2 o lo forzamos abajo
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.business.tasa-iva=0.19", // Asegurar que el IVA esté configurado
        "application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000"
})
class PedidoRefactorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AutoPartRepository autoPartRepository;
    @Autowired
    private IPedidoRepository pedidoRepository;
    @Autowired
    private IClienteRepository clienteRepository;
    @Autowired
    private ObjectMapper objectMapper;

    // MySQLContainer eliminado

    private Cliente cliente;
    private AutoPart prodA;
    private AutoPart prodB;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        autoPartRepository.deleteAll();
        clienteRepository.deleteAll();

        // Categoria eliminada, AutoPart usa campos directos

        cliente = new Cliente();
        cliente.setNit("101010");
        cliente.setNombre("Cliente Refactor");
        cliente = clienteRepository.save(cliente);

        prodA = new AutoPart();
        prodA.setId("A001");
        prodA.setDescription("Producto A");
        prodA.setPrice(new BigDecimal("100.00"));
        prodA.setStock(50); // Stock Inicial: 50
        prodA.setProductType("TYPE_A");
        prodA = autoPartRepository.save(prodA);

        prodB = new AutoPart();
        prodB.setId("B001");
        prodB.setDescription("Producto B");
        prodB.setPrice(new BigDecimal("200.00"));
        prodB.setStock(30); // Stock Inicial: 30
        prodB.setProductType("TYPE_B");
        prodB = autoPartRepository.save(prodB);
    }

    @Test
    @DisplayName("Refactor: Crear pedido debe descontar stock y calcular IVA")
    @WithMockUser(authorities = "ADMIN")
    void crearPedido_DebeDescontarStockYCalcularIva() throws Exception {
        // Pedido: 2 unidades de A
        DetallePedidoRequestDTO item1 = new DetallePedidoRequestDTO();
        item1.setAutoPartId("A001");
        item1.setCantidad(2);

        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setClienteId(cliente.getId());
        dto.setAsesorId(1L);
        dto.setItems(Collections.singletonList(item1));

        String responseBody = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PedidoResponseDTO pedido = objectMapper.readValue(responseBody, PedidoResponseDTO.class);

        // 1. Verificar Cálculos (IVA 19%)
        // Subtotal = 100 * 2 = 200
        // IVA = 200 * 0.19 = 38
        // Total = 238
        assertThat(pedido.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(pedido.getIva()).isEqualByComparingTo("38.00");
        assertThat(pedido.getTotal()).isEqualByComparingTo("238.00");

        // 2. Verificar Stock en BD
        AutoPart prodA_DB = autoPartRepository.findById("A001").orElseThrow();
        // Inicial 50 - 2 = 48
        assertThat(prodA_DB.getStock()).isEqualTo(48);
    }

    @Test
    @DisplayName("Refactor: Actualizar pedido debe devolver stock previo y descontar nuevo")
    @WithMockUser(authorities = "ADMIN")
    void actualizarPedido_DebeManejarStockCorrectamente() throws Exception {
        // --- PASO 1: Crear pedido inicial (5 items de A) ---
        DetallePedidoRequestDTO itemInicial = new DetallePedidoRequestDTO();
        itemInicial.setAutoPartId("A001");
        itemInicial.setCantidad(5);

        PedidoRequestDTO createDto = new PedidoRequestDTO();
        createDto.setClienteId(cliente.getId());
        createDto.setAsesorId(1L);
        createDto.setItems(Collections.singletonList(itemInicial));

        String res = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long pedidoId = objectMapper.readValue(res, PedidoResponseDTO.class).getId();

        // Check Stock Intermedio: A debía bajar de 50 a 45
        AutoPart prodA_Intermedio = autoPartRepository.findById("A001").orElseThrow();
        assertThat(prodA_Intermedio.getStock()).isEqualTo(45);

        // --- PASO 2: Actualizar pedido ---
        // Cambiamos A de 5 a 2 (Debe devolver 3 al stock)
        // Agregamos B con 10 (Debe restar 10 al stock)
        DetallePedidoRequestDTO itemEditadoA = new DetallePedidoRequestDTO();
        itemEditadoA.setAutoPartId("A001");
        itemEditadoA.setCantidad(2);

        DetallePedidoRequestDTO itemNuevoB = new DetallePedidoRequestDTO();
        itemNuevoB.setAutoPartId("B001");
        itemNuevoB.setCantidad(10);

        PedidoRequestDTO updateDto = new PedidoRequestDTO();
        updateDto.setClienteId(cliente.getId());
        updateDto.setAsesorId(1L);
        updateDto.setItems(Arrays.asList(itemEditadoA, itemNuevoB));

        mockMvc.perform(put("/api/pedidos/" + pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        // --- PASO 3: Verificacion Final de Stock ---

        // Producto A:
        // Inicial: 50
        // Crear(-5) -> 45
        // Update:
        // Devolver(+5) -> 50 (Teórico, al limpiar detalles)
        // Restar(-2) -> 48
        // FINAL ESPERADO: 48
        AutoPart prodA_Final = autoPartRepository.findById("A001").orElseThrow();
        assertThat(prodA_Final.getStock()).isEqualTo(48);

        // Producto B:
        // Inicial: 30
        // Crear(0) -> 30
        // Update:
        // Restar(-10) -> 20
        // FINAL ESPERADO: 20
        AutoPart prodB_Final = autoPartRepository.findById("B001").orElseThrow();
        assertThat(prodB_Final.getStock()).isEqualTo(20);
    }
}
