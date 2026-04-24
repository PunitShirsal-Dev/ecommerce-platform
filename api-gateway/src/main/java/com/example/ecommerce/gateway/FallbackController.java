package com.example.ecommerce.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    public static final String ERROR = "error";

    @GetMapping(path = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Map<String, Object>>> productFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(ERROR, "Product service temporarily unavailable")));
    }

    @GetMapping(path = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Map<String, Object>>> orderFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(ERROR, "Order service temporarily unavailable")));
    }

    @GetMapping(path = "/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Map<String, Object>>> inventoryFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(ERROR, "Inventory service temporarily unavailable")));
    }

    @GetMapping(path = "/payment", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Map<String, Object>>> paymentFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(ERROR, "Payment service temporarily unavailable")));
    }

    @GetMapping(path = "/customer", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Map<String, Object>>> customerFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(ERROR, "Customer service temporarily unavailable")));
    }
}
