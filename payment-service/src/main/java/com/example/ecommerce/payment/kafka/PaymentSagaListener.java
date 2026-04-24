package com.example.ecommerce.payment.kafka;

import com.example.ecommerce.events.InventoryReservedPayload;
import com.example.ecommerce.events.SagaEventType;
import com.example.ecommerce.payment.service.PaymentSagaProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSagaListener {

    private final PaymentSagaProcessor processor;
    private final ObjectMapper objectMapper;

    public PaymentSagaListener(PaymentSagaProcessor processor, ObjectMapper objectMapper) {
        this.processor = processor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.saga-topic}", groupId = "${spring.application.name}-saga")
    public void onEvent(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        SagaEventType type = SagaEventType.valueOf(root.get("type").asText());
        if (type != SagaEventType.INVENTORY_RESERVED) {
            return;
        }
        String eventId = root.get("eventId").asText();
        String orderId = root.get("orderId").asText();
        InventoryReservedPayload payload = objectMapper.treeToValue(root.get("payload"), InventoryReservedPayload.class);
        processor.onInventoryReserved(eventId, orderId, payload);
    }
}
