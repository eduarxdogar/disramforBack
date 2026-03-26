package com.disramfor.api.service;

import com.disramfor.api.dto.ClienteRequestDTO;
import com.disramfor.api.dto.ClienteResponseDTO;
import com.disramfor.api.dto.IClienteMapper;
import com.disramfor.api.entity.Cliente;
import com.disramfor.api.entity.Rol;
import com.disramfor.api.entity.Usuario;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private IClienteRepository clienteRepository;
    @Mock
    private IClienteMapper clienteMapper;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @InjectMocks
    private ClienteService clienteService;

    // Datos de prueba
    private Cliente clienteEntidad;
    private ClienteRequestDTO clienteRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;
    private Usuario usuarioAsesor;

    @BeforeEach
    void setUp() {
        // Objeto Usuario de prueba que será el "asesor"
        usuarioAsesor = new Usuario(1L, "asesorTest", "asesor@test.com", "password", Rol.ASESOR, null);

        // --- CORRECCIÓN 1: El DTO de petición ahora DEBE llevar el ID del asesor ---
        // Esto es lo que el método 'crear' espera recibir.
        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNit("123456789");
        clienteRequestDTO.setNombre("Empresa Test S.A.S");
        clienteRequestDTO.setAsesorId(1L); // Le asignamos el ID de nuestro asesor de prueba.

        // Entidad Cliente que se "guardará" en la base de datos
        clienteEntidad = new Cliente();
        clienteEntidad.setId(1L);
        clienteEntidad.setAsesor(usuarioAsesor);
        clienteEntidad.setNombre("Empresa Test S.A.S");

        // DTO de respuesta que el servicio devolverá
        clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setId(1L);
        clienteResponseDTO.setAsesorId(usuarioAsesor.getId());
        clienteResponseDTO.setAsesorNombre(usuarioAsesor.getNombreUsuario());
    }

    @Test
    @DisplayName("Debe crear un cliente correctamente")
    void crear_DebeCrearCliente_ConDatosValidos() {
        // Arrange (Preparar la simulación)

        // --- CORRECCIÓN 2: Simular la búsqueda que el servicio REALMENTE hace ---
        // El servicio llamará a findById con el ID que viene en el DTO (1L).
        // Preparamos el mock para que cuando eso ocurra, devuelva nuestro asesor de prueba.
        when(usuarioRepository.findById(eq(1L))).thenReturn(Optional.of(usuarioAsesor));

        // Mantenemos las otras simulaciones necesarias
        when(clienteMapper.toEntity(any(ClienteRequestDTO.class))).thenReturn(clienteEntidad);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEntidad);
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(clienteResponseDTO);

        // Act (Ejecutar el método a probar)
        // Le pasamos el DTO que ahora sí contiene el ID del asesor.
        ClienteResponseDTO resultado = clienteService.crear(clienteRequestDTO);

        // Assert (Verificar el resultado)
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAsesorId());
        assertEquals("asesorTest", resultado.getAsesorNombre());

        // Verificamos que se llamó a los métodos correctos
        verify(usuarioRepository).findById(eq(1L));
        verify(clienteRepository).save(any(Cliente.class));
    }
}