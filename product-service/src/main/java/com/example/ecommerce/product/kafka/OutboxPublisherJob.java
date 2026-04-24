package com.example.ecommerce.product.kafka;

import com.example.ecommerce.events.KafkaTopics;
import com.example.ecommerce.events.ProductIndexEvent;
import com.example.ecommerce.product.domain.OutboxMessage;
import com.example.ecommerce.product.repo.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisherJob(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:1000}")
    public void publishPending() {
        for (OutboxMessage msg : outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc()) {
            try {
                objectMapper.readValue(msg.getPayloadJson(), ProductIndexEvent.class);
                kafkaTemplate.send(KafkaTopics.PRODUCT_INDEX, msg.getAggregateId(), msg.getPayloadJson()).join();
                msg.setPublished(true);
                outboxRepository.save(msg);
            } catch (Exception e) {
                log.warn("Outbox publish failed id={}", msg.getId(), e);
            }
        }
    }
}
