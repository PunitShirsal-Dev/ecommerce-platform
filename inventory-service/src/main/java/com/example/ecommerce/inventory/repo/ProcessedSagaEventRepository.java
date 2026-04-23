package com.example.ecommerce.inventory.repo;

import com.example.ecommerce.inventory.domain.ProcessedSagaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSagaEventRepository extends JpaRepository<ProcessedSagaEvent, String> {
}
