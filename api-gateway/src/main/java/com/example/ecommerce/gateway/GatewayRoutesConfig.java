package com.example.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            RedisRateLimiter redisRateLimiter,
            KeyResolver userOrIpKeyResolver) {
        return builder.routes()
                .route("product-openapi", r -> r
                        .path("/aggregate/openapi/product")
                        .filters(f -> f.rewritePath("/aggregate/openapi/product", "/v3/api-docs"))
                        .uri("lb://product-service"))
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userOrIpKeyResolver))
                                .circuitBreaker(c -> c.setName("productService").setFallbackUri("forward:/fallback/product")))
                        .uri("lb://product-service"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userOrIpKeyResolver))
                                .circuitBreaker(c -> c.setName("orderService").setFallbackUri("forward:/fallback/order")))
                        .uri("lb://order-service"))
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userOrIpKeyResolver))
                                .circuitBreaker(c -> c.setName("inventoryService").setFallbackUri("forward:/fallback/inventory")))
                        .uri("lb://inventory-service"))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userOrIpKeyResolver))
                                .circuitBreaker(c -> c.setName("paymentService").setFallbackUri("forward:/fallback/payment")))
                        .uri("lb://payment-service"))
                .route("customer-service", r -> r
                        .path("/api/customers/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userOrIpKeyResolver))
                                .circuitBreaker(c -> c.setName("customerService").setFallbackUri("forward:/fallback/customer")))
                        .uri("lb://customer-service"))
                .build();
    }
}
