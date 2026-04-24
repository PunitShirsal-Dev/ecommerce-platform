package com.example.ecommerce.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "inventory-service", path = "/api/inventory", configuration = FeignTokenRelayConfig.class)
public interface InventoryClient {

    @GetMapping("/{productId}")
    Map<String, Object> getStock(@PathVariable("productId") String productId);
}
