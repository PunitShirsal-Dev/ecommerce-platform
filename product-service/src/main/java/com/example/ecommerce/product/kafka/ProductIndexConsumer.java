package com.example.ecommerce.product.kafka;

import com.example.ecommerce.events.KafkaTopics;
import com.example.ecommerce.events.ProductIndexEvent;
import com.example.ecommerce.product.search.ProductSearchDocument;
import com.example.ecommerce.product.search.ProductSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ProductIndexConsumer {

    private final ProductSearchRepository searchRepository;
    private final ObjectMapper objectMapper;

    public ProductIndexConsumer(ProductSearchRepository searchRepository, ObjectMapper objectMapper) {
        this.searchRepository = searchRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_INDEX,
            groupId = "${spring.application.name}-indexer",
            containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(
            @Payload String json,
            @Header(KafkaHeaders.RECEIVED_KEY) String productId,
            Acknowledgment ack) {
        try {
            ProductIndexEvent event = objectMapper.readValue(json, ProductIndexEvent.class);
            if (ProductIndexEvent.DELETE.equals(event.action())) {
                searchRepository.deleteById(event.productId());
            } else {
                ProductSearchDocument doc = new ProductSearchDocument();
                doc.setId(event.productId());
                doc.setName(event.name());
                doc.setDescription(event.description());
                doc.setCategory(event.category());
                doc.setPrice(event.price());
                searchRepository.save(doc);
            }
            ack.acknowledge();
        } catch (Exception e) {
            throw new IllegalStateException("Index consumer failed for productId=" + productId, e);
        }
    }
}
