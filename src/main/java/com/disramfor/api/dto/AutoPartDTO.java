package com.disramfor.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoPartDTO {
    private String id; // Changed to String to match 'codigo'
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private String productType;
    private String brand;
    private String model;
    private String engine;
}
