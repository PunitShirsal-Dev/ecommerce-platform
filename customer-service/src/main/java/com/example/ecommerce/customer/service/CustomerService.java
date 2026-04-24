package com.example.ecommerce.customer.service;

import com.example.ecommerce.customer.domain.Customer;
import com.example.ecommerce.customer.dot.CustomerRequest;
import com.example.ecommerce.customer.repo.CustomerRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerService self;
    private final Cache<String, Customer> local = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public CustomerService(@Lazy CustomerService self, CustomerRepository customerRepository) {
        this.self = self;
        this.customerRepository = customerRepository;
    }

    /**
     * L1 Caffeine + L2 Redis via proxied {@link #loadThroughCache(String)}.
     */
    public Customer get(String id) {
        return local.get(id, self::loadThroughCache);
    }

    @Cacheable(cacheNames = "customers", key = "#id")
    public Customer loadThroughCache(String id) {
        return customerRepository.findById(id).orElseThrow();
    }

    public List<Customer> list() {
        return customerRepository.findAll();
    }

    @Transactional
    @CacheEvict(cacheNames = "customers", key = "#customerRequest.id")
    public Customer save(CustomerRequest customerRequest) {
        Customer customer = new Customer(customerRequest.getId(), customerRequest.getEmail(), customerRequest.getFullName(), customerRequest.getPreferredCurrency());
        Customer saved = customerRepository.save(customer);
        local.invalidate(saved.getId());
        return saved;
    }
}
