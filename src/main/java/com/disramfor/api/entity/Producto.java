package com.disramfor.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String pasillo;
    private Integer nivel;
    private Integer espacio;
    private BigDecimal precioUnitario;
}

