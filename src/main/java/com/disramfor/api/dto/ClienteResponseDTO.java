package com.disramfor.api.dto;

import com.disramfor.api.entity.Cliente;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para las respuestas de Cliente.
 */
@Data
public class ClienteResponseDTO {
    private Long id;
    private String nit;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private String email;
    private BigDecimal descuento;

    // CAMPOS AGREGADOS PARA MOSTRAR LA INFORMACIÓN DEL ASESOR
    private Long asesorId;
    private String asesorNombre;


}
