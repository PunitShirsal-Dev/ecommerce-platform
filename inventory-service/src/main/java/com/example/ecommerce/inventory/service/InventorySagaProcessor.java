package com.example.ecommerce.inventory.service;

import com.example.ecommerce.events.InventoryInsufficientPayload;
import com.example.ecommerce.events.InventoryReservedPayload;
import com.example.ecommerce.events.KafkaTopics;
import com.example.ecommerce.events.OrderPlacedPayload;
import com.example.ecommerce.events.SagaEnvelope;
import com.example.ecommerce.events.SagaEventType;
import com.example.ecommerce.inventory.domain.ProcessedSagaEvent;
import com.example.ecommerce.inventory.domain.Reservation;
import com.example.ecommerce.inventory.domain.StockItem;
import com.example.ecommerce.inventory.repo.ProcessedSagaEventRepository;
import com.example.ecommerce.inventory.repo.ReservationRepository;
import com.example.ecommerce.inventory.repo.StockItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InventorySagaProcessor {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaProcessor.class);

    private final StockItemRepository stockItemRepository;
    private final ReservationRepository reservationRepository;
    private final ProcessedSagaEventRepository processedSagaEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InventorySagaProcessor(
            StockItemRepository stockItemRepository,
            ReservationRepository reservationRepository,
            ProcessedSagaEventRepository processedSagaEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.stockItemRepository = stockItemRepository;
        this.reservationRepository = reservationRepository;
        this.processedSagaEventRepository = processedSagaEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handleOrderPlaced(String eventId, String orderId, OrderPlacedPayload payload) throws Exception {
        if (processedSagaEventRepository.existsById(eventId)) {
            return;
        }
        List<OrderPlacedPayload.OrderLinePayload> lines = new ArrayList<>(payload.lines());
        lines.sort(Comparator.comparing(OrderPlacedPayload.OrderLinePayload::productId));

        List<StockItem> locked = new ArrayList<>();
        for (OrderPlacedPayload.OrderLinePayload line : lines) {
            StockItem stock = stockItemRepository.findForUpdate(line.productId())
                    .orElseGet(() -> {
                        StockItem s = new StockItem();
                        s.setProductId(line.productId());
                        s.setQuantityOnHand(0);
                        return stockItemRepository.save(s);
                    });
            if (stock.getQuantityOnHand() < line.quantity()) {
                publish(SagaEnvelope.of(
                        SagaEventType.INVENTORY_INSUFFICIENT,
                        orderId,
                        new InventoryInsufficientPayload(line.productId(), line.quantity(), stock.getQuantityOnHand())));
                markProcessed(eventId);
                return;
            }
            locked.add(stock);
        }

        List<String> reservationIds = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            OrderPlacedPayload.OrderLinePayload line = lines.get(i);
            StockItem stock = locked.get(i);
            stock.setQuantityOnHand(stock.getQuantityOnHand() - line.quantity());
            stockItemRepository.save(stock);
            Reservation r = new Reservation();
            r.setOrderId(orderId);
            r.setProductId(line.productId());
            r.setQuantity(line.quantity());
            reservationRepository.save(r);
            reservationIds.add(String.valueOf(r.getId()));
        }
        publish(SagaEnvelope.of(
                SagaEventType.INVENTORY_RESERVED,
                orderId,
                new InventoryReservedPayload(reservationIds, payload.totalAmount())));
        markProcessed(eventId);
    }

    @Transactional
    public void handlePaymentFailed(String eventId, String orderId) throws Exception {
        if (processedSagaEventRepository.existsById(eventId)) {
            return;
        }
        releaseStockForOrder(orderId);
        publish(SagaEnvelope.of(SagaEventType.INVENTORY_RELEASED, orderId, null));
        markProcessed(eventId);
    }

    @Transactional
    public void handlePaymentProcessed(String eventId, String orderId) throws Exception {
        if (processedSagaEventRepository.existsById(eventId)) {
            return;
        }
        reservationRepository.deleteByOrderId(orderId);
        markProcessed(eventId);
    }

    private void releaseStockForOrder(String orderId) {
        for (Reservation r : reservationRepository.findByOrderId(orderId)) {
            StockItem stock = stockItemRepository.findForUpdate(r.getProductId()).orElseThrow();
            stock.setQuantityOnHand(stock.getQuantityOnHand() + r.getQuantity());
            stockItemRepository.save(stock);
        }
        reservationRepository.deleteByOrderId(orderId);
    }

    private void markProcessed(String eventId) {
        ProcessedSagaEvent e = new ProcessedSagaEvent();
        e.setEventId(eventId);
        e.setProcessedAt(Instant.now());
        processedSagaEventRepository.save(e);
    }

    private void publish(SagaEnvelope envelope) throws Exception {
        String json = objectMapper.writeValueAsString(envelope);
        kafkaTemplate.send(KafkaTopics.SAGA_EVENTS, envelope.orderId(), json).join();
        log.debug("Published saga {}", envelope.type());
    }
}
