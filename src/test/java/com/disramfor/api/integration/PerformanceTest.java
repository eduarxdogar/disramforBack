package com.disramfor.api.integration;

import com.disramfor.api.entity.Categoria;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.entity.Producto;
import com.disramfor.api.repository.ICategoriaRepository;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IProductoRepository;
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
@Testcontainers
@ActiveProfiles("integration-test")
@Transactional
class PerformanceTest {

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

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
        productoRepository.deleteAll();
        clienteRepository.deleteAll();
        categoriaRepository.deleteAll();
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
         //   cliente.setAsesor("Asesor " + (i % 10)); // 10 asesores diferentes
            clientes.add(cliente);
        }
        clienteRepository.saveAll(clientes);

        // Medir tiempo de búsqueda paginada
        long startTime = System.currentTimeMillis();

        Page<Cliente> resultado = clienteRepository.searchByTerm(
                "Cliente",
                PageRequest.of(0, 50)
        );

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
    @DisplayName("Debe buscar productos eficientemente por categoría")
    void debeBuscarProductosEficientemente() throws Exception {
        // Crear categorías
        Categoria categoria1 = new Categoria(null, "Electrónicos");
        Categoria categoria2 = new Categoria(null, "Hogar");
        categoria1 = categoriaRepository.save(categoria1);
        categoria2 = categoriaRepository.save(categoria2);

        // Crear 2000 productos
        List<Producto> productos = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            Producto producto = new Producto();
            producto.setCodigo("PROD" + String.format("%06d", i));
            producto.setNombre("Producto " + i);
            producto.setPrecioUnitario(new BigDecimal("" + (10 + i % 100)));
            producto.setCategoria(i % 2 == 0 ? categoria1 : categoria2);
            productos.add(producto);
        }
        productoRepository.saveAll(productos);

        // Medir tiempo de búsqueda por categoría
        long startTime = System.currentTimeMillis();

        Page<Producto> resultado = productoRepository.findAll(
          //      (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoria1.getId()),
                PageRequest.of(0, 100)
        );

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
