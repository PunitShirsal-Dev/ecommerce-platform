package com.example.ecommerce.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderPlacedPayload(
        @NotBlank String customerId,
        @NotNull List<OrderLinePayload> lines,
        @NotNull BigDecimal totalAmount
) {
    public record OrderLinePayload(
            @NotBlank String productId,
            @NotNull Integer quantity,
            @NotNull BigDecimal unitPrice
    ) {
    }
}
