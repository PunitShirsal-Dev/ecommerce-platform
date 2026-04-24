package com.example.ecommerce.payment.config;

import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${app.stripe.api-key:}")
    private String stripeApiKey;

    @Bean
    public RequestOptions stripeRequestOptions() {
        return RequestOptions.builder()
                .setApiKey(stripeApiKey)
                .build();
    }
}
