package com.example.ecommerce.product.repo;

import com.example.ecommerce.product.domain.OutboxMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxRepository extends MongoRepository<OutboxMessage, String> {

    List<OutboxMessage> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}
