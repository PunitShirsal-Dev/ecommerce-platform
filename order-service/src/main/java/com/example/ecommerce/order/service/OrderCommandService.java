package com.example.ecommerce.order.service;

import com.example.ecommerce.events.KafkaTopics;
import com.example.ecommerce.events.OrderPlacedPayload;
import com.example.ecommerce.events.SagaEnvelope;
import com.example.ecommerce.events.SagaEventType;
import com.example.ecommerce.order.domain.OrderEntity;
import com.example.ecommerce.order.domain.OrderLineEntity;
import com.example.ecommerce.order.domain.OrderStatus;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.order.web.dto.PlaceOrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderCommandService(
            OrderRepository orderRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderEntity placeOrder(PlaceOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (PlaceOrderRequest.Line line : request.lines()) {
            OrderLineEntity ol = new OrderLineEntity();
            ol.setProductId(line.productId());
            ol.setQuantity(line.quantity());
            ol.setUnitPrice(line.unitPrice());
            ol.setOrder(order);
            order.getLines().add(ol);
            total = total.add(line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        order.setTotalAmount(total);
        orderRepository.save(order);

        var payloadLines = request.lines().stream()
                .map(l -> new OrderPlacedPayload.OrderLinePayload(l.productId(), l.quantity(), l.unitPrice()))
                .toList();
        OrderPlacedPayload payload = new OrderPlacedPayload(request.customerId(), payloadLines, total);
        SagaEnvelope envelope = SagaEnvelope.of(SagaEventType.ORDER_PLACED, orderId, payload);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    String json = objectMapper.writeValueAsString(envelope);
                    kafkaTemplate.send(KafkaTopics.SAGA_EVENTS, orderId, json).join();
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        return order;
    }
}
