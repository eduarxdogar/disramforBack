package com.disramfor.api.dto;

import com.disramfor.api.entity.Cliente;
import lombok.Data;

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
    private String asesor;


}
