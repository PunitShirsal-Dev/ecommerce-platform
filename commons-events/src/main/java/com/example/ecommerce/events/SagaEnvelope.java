package com.example.ecommerce.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SagaEnvelope(
        String eventId,
        SagaEventType type,
        String orderId,
        Instant occurredAt,
        Object payload
) {
    public static SagaEnvelope of(SagaEventType type, String orderId, Object payload) {
        return new SagaEnvelope(UUID.randomUUID().toString(), type, orderId, Instant.now(), payload);
    }
}
