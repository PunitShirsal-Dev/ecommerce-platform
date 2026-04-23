package com.example.ecommerce.webui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/oauth2/**", "/oauth2/**", "/error", "/actuator/health", "/css/**")
                        .permitAll()
                        .requestMatchers("/products", "/products/**")
                        .authenticated()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(o -> o.defaultSuccessUrl("/products", true))
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}
