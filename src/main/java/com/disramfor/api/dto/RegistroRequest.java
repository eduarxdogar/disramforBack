package com.disramfor.api.dto;


import com.disramfor.api.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistroRequest {

    private String usuarionombre;
    private String email;
    private String password;
    private Rol rol;
}
