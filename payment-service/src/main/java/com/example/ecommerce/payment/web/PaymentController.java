package com.example.ecommerce.payment.web;

import com.example.ecommerce.payment.domain.PaymentEntity;
import com.example.ecommerce.payment.repo.PaymentRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentEntity> list() {
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public PaymentEntity get(@PathVariable String id) {
        return paymentRepository.findById(id).orElseThrow();
    }
}
