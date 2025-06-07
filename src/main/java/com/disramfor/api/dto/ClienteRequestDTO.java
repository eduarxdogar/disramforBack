package com.disramfor.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para las peticiones de creación o actualización de Cliente.
 */
@Data
public class ClienteRequestDTO {

    @NotBlank(message = "El NIT es obligatorio")
    @Size(max = 20, message = "El NIT no puede exceder 20 caracteres")
    private String nit;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
    private String telefono;

    @Email(message = "El email no tiene el formato correcto")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
}
