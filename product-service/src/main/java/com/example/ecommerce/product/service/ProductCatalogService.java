package com.example.ecommerce.product.service;

import com.example.ecommerce.events.ProductIndexEvent;
import com.example.ecommerce.product.domain.OutboxMessage;
import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.repo.OutboxRepository;
import com.example.ecommerce.product.repo.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ProductCatalogService(
            ProductRepository productRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public List<Product> list() {
        return productRepository.findAll();
    }

    public Optional<Product> get(String id) {
        return productRepository.findById(id);
    }

    public Product upsert(Product product) {
        product.setUpdatedAt(Instant.now());
        Product saved = productRepository.save(product);
        enqueueIndexEvent(new ProductIndexEvent(
                saved.getId(),
                ProductIndexEvent.UPSERT,
                saved.getName(),
                saved.getDescription(),
                saved.getCategory(),
                saved.getPrice()));
        return saved;
    }

    public void delete(String id) {
        productRepository.deleteById(id);
        enqueueIndexEvent(new ProductIndexEvent(id, ProductIndexEvent.DELETE, null, null, null, null));
    }

    private void enqueueIndexEvent(ProductIndexEvent event) {
        try {
            OutboxMessage msg = new OutboxMessage();
            msg.setId(UUID.randomUUID().toString());
            msg.setAggregateId(event.productId());
            msg.setPayloadJson(objectMapper.writeValueAsString(event));
            msg.setPublished(false);
            msg.setCreatedAt(Instant.now());
            outboxRepository.save(msg);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
