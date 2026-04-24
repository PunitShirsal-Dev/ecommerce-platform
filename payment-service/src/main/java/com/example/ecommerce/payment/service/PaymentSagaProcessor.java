package com.example.ecommerce.payment.service;

import com.example.ecommerce.events.*;
import com.example.ecommerce.payment.domain.PaymentEntity;
import com.example.ecommerce.payment.domain.PaymentStatus;
import com.example.ecommerce.payment.domain.ProcessedSagaEvent;
import com.example.ecommerce.payment.repo.PaymentRepository;
import com.example.ecommerce.payment.repo.ProcessedSagaEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentSagaProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaProcessor.class);

    private final PaymentRepository paymentRepository;
    private final ProcessedSagaEventRepository processedSagaEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RequestOptions requestOptions;

    @Value("${app.stripe.api-key:}")
    private String stripeApiKey;

    public PaymentSagaProcessor(
            PaymentRepository paymentRepository,
            ProcessedSagaEventRepository processedSagaEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            RequestOptions requestOptions) {
        this.paymentRepository = paymentRepository;
        this.processedSagaEventRepository = processedSagaEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.requestOptions = requestOptions;
    }

    @Transactional
    public void onInventoryReserved(String eventId, String orderId, InventoryReservedPayload payload) throws JsonProcessingException {
        if (processedSagaEventRepository.existsById(eventId)) {
            return;
        }
        String paymentId = UUID.randomUUID().toString();
        PaymentEntity payment = new PaymentEntity();
        payment.setId(paymentId);
        payment.setOrderId(orderId);
        payment.setAmount(payload.orderTotal());
        payment.setCreatedAt(Instant.now());

        try {
            String reference = chargeProvider(payload);
            payment.setStatus(PaymentStatus.AUTHORIZED);
            payment.setProviderReference(reference);
            paymentRepository.save(payment);
            publish(SagaEnvelope.of(
                    SagaEventType.PAYMENT_PROCESSED,
                    orderId,
                    new PaymentProcessedPayload(paymentId, reference)));
        } catch (Exception ex) {
            log.warn("Payment failed for order {}", orderId, ex);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            publish(SagaEnvelope.of(
                    SagaEventType.PAYMENT_FAILED,
                    orderId,
                    new PaymentFailedPayload(ex.getMessage())));
        }
        markProcessed(eventId);
    }

    private String chargeProvider(InventoryReservedPayload payload) throws StripeException {
        if (stripeApiKey == null || stripeApiKey.isBlank() || stripeApiKey.contains("placeholder")) {
            return "simulated_pi_" + UUID.randomUUID();
        }
        long amountCents = payload.orderTotal().movePointRight(2).longValueExact();
        PaymentIntent intent = PaymentIntent.create(
                PaymentIntentCreateParams.builder()
                        .setAmount(amountCents)
                        .setCurrency("usd")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build())
                        .build(),
                requestOptions
                );
        return intent.getId();
    }

    private void markProcessed(String eventId) {
        ProcessedSagaEvent e = new ProcessedSagaEvent();
        e.setEventId(eventId);
        e.setProcessedAt(Instant.now());
        processedSagaEventRepository.save(e);
    }

    private void publish(SagaEnvelope envelope) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(envelope);
        kafkaTemplate.send(KafkaTopics.SAGA_EVENTS, envelope.orderId(), json).join();
    }
}
