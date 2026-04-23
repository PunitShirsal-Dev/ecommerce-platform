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

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.gateway-base-url:http://api-gateway}")
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
    public String products(Model model, Authentication authentication) {
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(REGISTRATION_ID, authentication.getName());
        if (client == null || client.getAccessToken() == null) {
            model.addAttribute("error", "Not signed in with OAuth2.");
            model.addAttribute("products", Collections.<Map<String, Object>>emptyList());
            return "products";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(client.getAccessToken().getTokenValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = gatewayBaseUrl + "/api/products";
        try {
            List<Map<String, Object>> body = restTemplate
                    .exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .getBody();
            model.addAttribute("products", body != null ? body : Collections.emptyList());
            model.addAttribute("error", null);
        } catch (Exception ex) {
            model.addAttribute("error", "Could not load catalog: " + ex.getMessage());
            model.addAttribute("products", Collections.<Map<String, Object>>emptyList());
        }
        return "products";
    }
}
