package com.example.ecommerce.inventory.kafka;

import com.example.ecommerce.events.OrderPlacedPayload;
import com.example.ecommerce.events.SagaEventType;
import com.example.ecommerce.inventory.service.InventorySagaProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventorySagaListener {

    private final InventorySagaProcessor processor;
    private final ObjectMapper objectMapper;

    public InventorySagaListener(InventorySagaProcessor processor, ObjectMapper objectMapper) {
        this.processor = processor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.saga-topic}", groupId = "${spring.application.name}-saga")
    public void onEvent(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        SagaEventType type = SagaEventType.valueOf(root.get("type").asText());
        String eventId = root.get("eventId").asText();
        String orderId = root.get("orderId").asText();
        switch (type) {
            case ORDER_PLACED -> {
                OrderPlacedPayload payload = objectMapper.treeToValue(root.get("payload"), OrderPlacedPayload.class);
                processor.handleOrderPlaced(eventId, orderId, payload);
            }
            case PAYMENT_FAILED -> processor.handlePaymentFailed(eventId, orderId);
            case PAYMENT_PROCESSED -> processor.handlePaymentProcessed(eventId, orderId);
            default -> {
                // No action needed for this event type; it is expected and can be safely ignored.
            }
        }
    }
}
