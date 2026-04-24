package com.example.ecommerce.order.kafka;

import com.example.ecommerce.events.SagaEventType;
import com.example.ecommerce.order.domain.OrderStatus;
import com.example.ecommerce.order.domain.ProcessedSagaEvent;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.order.repo.ProcessedSagaEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OrderSagaListener {

    private final OrderRepository orderRepository;
    private final ProcessedSagaEventRepository processedSagaEventRepository;
    private final ObjectMapper objectMapper;

    public OrderSagaListener(
            OrderRepository orderRepository,
            ProcessedSagaEventRepository processedSagaEventRepository,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.processedSagaEventRepository = processedSagaEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.saga-topic}", groupId = "${spring.application.name}-saga")
    @Transactional
    public void onSagaEvent(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        String eventId = root.get("eventId").asText();
        if (processedSagaEventRepository.existsById(eventId)) {
            return;
        }
        SagaEventType type = SagaEventType.valueOf(root.get("type").asText());
        String orderId = root.get("orderId").asText();

        boolean handled = switch (type) {
            case INVENTORY_INSUFFICIENT, PAYMENT_FAILED -> {
                cancel(orderId, OrderStatus.CANCELLED);
                yield true;
            }
            case PAYMENT_PROCESSED -> {
                transition(orderId, OrderStatus.COMPLETED);
                yield true;
            }
            case INVENTORY_RESERVED -> {
                transition(orderId, OrderStatus.RESERVED);
                yield true;
            }
            default -> false;
        };

        if (!handled) {
            return;
        }

        ProcessedSagaEvent pe = new ProcessedSagaEvent();
        pe.setEventId(eventId);
        pe.setProcessedAt(Instant.now());
        processedSagaEventRepository.save(pe);
    }

    private void transition(String orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(o -> {
            o.setStatus(status);
            orderRepository.save(o);
        });
    }

    private void cancel(String orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(o -> {
            if (o.getStatus() == OrderStatus.COMPLETED) {
                return;
            }
            o.setStatus(status);
            orderRepository.save(o);
        });
    }
}
