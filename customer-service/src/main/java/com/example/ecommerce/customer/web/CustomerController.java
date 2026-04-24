package com.example.ecommerce.customer.web;

import com.example.ecommerce.customer.domain.Customer;
import com.example.ecommerce.customer.dot.CustomerRequest;
import com.example.ecommerce.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Customer> list() {
        return customerService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Customer get(@PathVariable String id) {
        return customerService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Customer create(@RequestBody CustomerRequest customerRequest) {
        customerRequest.setId(UUID.randomUUID().toString());
        return customerService.save(customerRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Customer update(@PathVariable String id, @RequestBody CustomerRequest customerRequest) {
        customerRequest.setId(id);
        return customerService.save(customerRequest);
    }
}
