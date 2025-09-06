package com.disramfor.api.entity;
import com.disramfor.api.dto.ClienteRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliente_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String ciudad;
    private String telefono;
    private String email;
    private String asesor;

    @Column(precision = 5, scale = 2)
    private BigDecimal descuento;
}
