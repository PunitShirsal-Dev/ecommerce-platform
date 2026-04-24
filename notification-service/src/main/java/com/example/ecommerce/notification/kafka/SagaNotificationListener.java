package com.example.ecommerce.notification.kafka;

import com.example.ecommerce.events.SagaEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SagaNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(SagaNotificationListener.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public SagaNotificationListener(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.saga-topic}", groupId = "${spring.application.name}-notify")
    public void onSaga(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        String eventId = root.get("eventId").asText();
        Boolean first = redis.opsForValue().setIfAbsent(
                "notif:dedupe:" + eventId,
                "1",
                Duration.ofDays(7));
        if (Boolean.FALSE.equals(first)) {
            return;
        }
        SagaEventType type = SagaEventType.valueOf(root.get("type").asText());
        String orderId = root.get("orderId").asText();
        log.info("Notification channel=sms orderId={} event={}", orderId, type);
    }
}
