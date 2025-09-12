package com.disramfor.api.entity;

import jakarta.persistence.*;
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
    @Column(name = "imagen_url")
    private String imagenUrl;
    private String pasillo;
    private Integer nivel;
    private Integer espacio;
    private BigDecimal precioUnitario;
    @Column(name = "stock_disponible", nullable = false, columnDefinition = "INT DEFAULT 100")
    private Integer stockDisponible;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}

