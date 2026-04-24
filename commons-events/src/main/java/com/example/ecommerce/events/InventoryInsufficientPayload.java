package com.example.ecommerce.events;

public record InventoryInsufficientPayload(
        String productId,
        int requested,
        int available
) {
}
