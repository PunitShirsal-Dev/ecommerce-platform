package com.example.ecommerce.order.repo;

import com.example.ecommerce.order.domain.ProcessedSagaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSagaEventRepository extends JpaRepository<ProcessedSagaEvent, String> {
}
