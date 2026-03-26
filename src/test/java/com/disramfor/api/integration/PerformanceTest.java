package com.disramfor.api.integration;

import com.disramfor.api.entity.AutoPart;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.repository.AutoPartRepository;
import com.disramfor.api.repository.IClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de rendimiento para verificar que las consultas sean eficientes
 */
@SpringBootTest
// @Testcontainers
@ActiveProfiles("integration-test")
@Transactional
@org.junit.jupiter.api.Disabled("Skipping performance tests due to missing Docker environment")
class PerformanceTest {

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private AutoPartRepository autoPartRepository;

    @SuppressWarnings("resource")
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("disramfor_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @BeforeEach
    void setUp() {
        // Limpiar datos
        autoPartRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe buscar clientes eficientemente con grandes volúmenes")
    void debeBuscarClientesEficientemente() throws Exception {
        // Crear 1000 clientes de prueba
        List<Cliente> clientes = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Cliente cliente = new Cliente();
            cliente.setNit("NIT" + String.format("%06d", i));
            cliente.setNombre("Cliente " + i);
            cliente.setCiudad(i % 2 == 0 ? "Bogotá" : "Medellín");
            clientes.add(cliente);
        }
        clienteRepository.saveAll(clientes);

        // Medir tiempo de búsqueda paginada
        long startTime = System.currentTimeMillis();

        Page<Cliente> resultado = clienteRepository.searchByTerm(
                "Cliente",
                PageRequest.of(0, 50));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verificaciones
        assertNotNull(resultado);
        assertTrue(resultado.getTotalElements() >= 1000);
        assertEquals(50, resultado.getContent().size());

        // La búsqueda no debe tomar más de 1 segundo
        assertTrue(duration < 1000,
                "La búsqueda tardó " + duration + "ms, esperado < 1000ms");

        System.out.println("✅ Búsqueda de clientes completada en " + duration + "ms");
    }

    @Test
    @DisplayName("Debe buscar productos eficientemente por tipo")
    void debeBuscarProductosEficientemente() throws Exception {
        // Crear 2000 productos
        List<AutoPart> productos = new ArrayList<>();
        String type1 = "TYPE_A";
        String type2 = "TYPE_B";

        for (int i = 0; i < 2000; i++) {
            AutoPart producto = new AutoPart();
            producto.setId("PROD" + String.format("%06d", i));
            producto.setDescription("Producto " + i);
            producto.setPrice(new BigDecimal("" + (10 + i % 100)));
            producto.setStock(100);
            producto.setProductType(i % 2 == 0 ? type1 : type2);
            producto.setBrand("BrandX");
            producto.setModel("ModelY");
            producto.setEngine("EngineZ");
            productos.add(producto);
        }
        autoPartRepository.saveAll(productos);

        // Medir tiempo de búsqueda por tipo
        long startTime = System.currentTimeMillis();

        // Usamos findAll con spec o simplemente count/find by type si existiera metodo
        // directo
        // Como AutoPartRepository es JpaSpecificationExecutor, podemos usar
        // findAll(spec)
        // Pero para simplificar, usaremos findAll con paginacion para simular carga

        Page<AutoPart> resultado = autoPartRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("productType"), type1),
                PageRequest.of(0, 100));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1000, resultado.getTotalElements()); // Mitad de los productos
        assertEquals(100, resultado.getContent().size());

        assertTrue(duration < 500,
                "La búsqueda tardó " + duration + "ms, esperado < 500ms");

        System.out.println("✅ Búsqueda de productos completada en " + duration + "ms");
    }
}
