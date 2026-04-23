package com.example.ecommerce.events;

public record PaymentProcessedPayload(
        String paymentId,
        String providerReference
) {
}
