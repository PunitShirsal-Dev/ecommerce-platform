package com.example.ecommerce.customer.repo;

import com.example.ecommerce.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
