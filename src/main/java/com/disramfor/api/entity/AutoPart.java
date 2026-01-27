package com.disramfor.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto", indexes = {
        @Index(name = "idx_part_type", columnList = "tipo_producto"),
        @Index(name = "idx_part_brand", columnList = "marca"),
        @Index(name = "idx_part_model", columnList = "modelo"),
        @Index(name = "idx_part_engine", columnList = "motor")
})
public class AutoPart {

    @Id
    @Column(name = "codigo", length = 50)
    private String id; // Mapped to 'codigo'

    @Column(name = "nombre", nullable = false, length = 500)
    private String description;

    @Column(name = "imagen_url", length = 500)
    private String imageUrl;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal price;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stock;

    // Hierarchy Filters - Mapped to Spanish columns
    @Column(name = "tipo_producto", length = 50)
    private String productType;

    @Column(name = "marca", length = 50)
    private String brand;

    @Column(name = "modelo", length = 50)
    private String model;

    @Column(name = "motor", length = 50)
    private String engine;
}
