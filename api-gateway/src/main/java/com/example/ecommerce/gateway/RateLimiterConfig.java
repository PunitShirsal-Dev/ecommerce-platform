package com.example.ecommerce.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    RedisRateLimiter redisRateLimiter(
            @Value("${app.rate-limit.replenish:20}") int replenish,
            @Value("${app.rate-limit.burst:40}") int burst) {
        return new RedisRateLimiter(replenish, burst, 1);
    }

    @Bean
    KeyResolver userOrIpKeyResolver() {
        return exchange -> {
            var auth = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (auth != null && auth.length() > 20) {
                return Mono.just("jwt:" + Integer.toHexString(auth.hashCode()));
            }
            var addr = exchange.getRequest().getRemoteAddress();
            if (addr != null && addr.getAddress() != null) {
                return Mono.just("ip:" + addr.getAddress().getHostAddress());
            }
            return Mono.just("anon");
        };
    }
}
