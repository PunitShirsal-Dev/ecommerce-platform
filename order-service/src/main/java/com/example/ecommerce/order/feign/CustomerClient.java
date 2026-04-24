package com.example.ecommerce.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "customer-service", path = "/api/customers", configuration = FeignTokenRelayConfig.class)
public interface CustomerClient {

    @GetMapping("/{id}")
    Map<String, Object> getCustomer(@PathVariable("id") String id);
}
