package com.example.ecommerce.payment.repo;

import com.example.ecommerce.payment.domain.ProcessedSagaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSagaEventRepository extends JpaRepository<ProcessedSagaEvent, String> {
}
