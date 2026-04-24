package com.example.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductRequest {

    private String id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Instant updatedAt;
}
