package com.example.ecommerce.events;

import java.math.BigDecimal;
import java.util.List;

public record InventoryReservedPayload(
        List<String> reservationIds,
        BigDecimal orderTotal
) {
}
