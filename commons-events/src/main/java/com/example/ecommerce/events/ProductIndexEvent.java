package com.example.ecommerce.events;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductIndexEvent(
        String productId,
        String action,
        String name,
        String description,
        String category,
        java.math.BigDecimal price
) {
    public static final String UPSERT = "UPSERT";
    public static final String DELETE = "DELETE";
}
