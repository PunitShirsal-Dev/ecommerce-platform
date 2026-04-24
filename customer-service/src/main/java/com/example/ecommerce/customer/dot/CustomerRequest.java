package com.example.ecommerce.customer.dot;

import lombok.Data;

@Data
public class CustomerRequest {
    private String id;
    private String email;
    private String fullName;
    private String preferredCurrency;
}
