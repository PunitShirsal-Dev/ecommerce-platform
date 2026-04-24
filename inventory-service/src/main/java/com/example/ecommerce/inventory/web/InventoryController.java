package com.example.ecommerce.inventory.web;

import com.example.ecommerce.inventory.domain.StockItem;
import com.example.ecommerce.inventory.repo.StockItemRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory")
public class InventoryController {

    private final StockItemRepository stockItemRepository;

    public InventoryController(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Map<String, Object> get(@PathVariable String productId) {
        StockItem s = stockItemRepository.findById(productId)
                .orElseGet(() -> {
                    StockItem n = new StockItem();
                    n.setProductId(productId);
                    n.setQuantityOnHand(0);
                    return stockItemRepository.save(n);
                });
        return Map.of("productId", s.getProductId(), "quantityOnHand", s.getQuantityOnHand());
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void setStock(@PathVariable String productId, @RequestBody Map<String, Integer> body) {
        int qty = body.getOrDefault("quantityOnHand", 0);
        StockItem s = stockItemRepository.findById(productId).orElseGet(() -> {
            StockItem n = new StockItem();
            n.setProductId(productId);
            return n;
        });
        s.setQuantityOnHand(qty);
        stockItemRepository.save(s);
    }
}
