package com.example.ecommerce.webui.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class ShopController {

    private static final String REGISTRATION_ID = "web-ui";
    public static final String ERROR = "error";
    public static final String PRODUCTS = "products";

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.gateway-base-url:https://api-gateway}")
    private String gatewayBaseUrl;

    public ShopController(RestTemplate restTemplate, OAuth2AuthorizedClientService authorizedClientService) {
        this.restTemplate = restTemplate;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/products")
    public void listProducts(Model model, Authentication authentication) {
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(REGISTRATION_ID, authentication.getName());
        if (client == null || client.getAccessToken() == null) {
            model.addAttribute(ERROR, "Not signed in with OAuth2.");
            model.addAttribute(PRODUCTS, Collections.emptyList());
            return; // no need to return a value
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(client.getAccessToken().getTokenValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = gatewayBaseUrl + "/api/products";
        try {
            List<Map<String, Object>> body = restTemplate
                    .exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .getBody();
            model.addAttribute(PRODUCTS, body != null ? body : Collections.emptyList());
            model.addAttribute(ERROR, null);
        } catch (Exception ex) {
            model.addAttribute(ERROR, "Could not load catalog: " + ex.getMessage());
            model.addAttribute(PRODUCTS, Collections.emptyList());
        }
    }
}
