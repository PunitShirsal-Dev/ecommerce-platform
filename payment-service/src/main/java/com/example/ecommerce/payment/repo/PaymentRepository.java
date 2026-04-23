package com.example.ecommerce.payment.repo;

import com.example.ecommerce.payment.domain.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
}
