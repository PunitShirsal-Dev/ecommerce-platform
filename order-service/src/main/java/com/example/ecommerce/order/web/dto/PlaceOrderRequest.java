package com.example.ecommerce.order.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlaceOrderRequest(
        String customerId,
        List<Line> lines
) {
    public record Line(
            String productId,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
