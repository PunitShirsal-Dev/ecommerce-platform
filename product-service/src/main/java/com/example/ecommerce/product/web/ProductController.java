package com.example.ecommerce.product.web;

import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.dto.ProductRequest;
import com.example.ecommerce.product.search.ProductSearchDocument;
import com.example.ecommerce.product.search.ProductSearchRepository;
import com.example.ecommerce.product.service.ProductCatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
public class ProductController {

    private final ProductCatalogService catalogService;
    private final ProductSearchRepository searchRepository;

    public ProductController(ProductCatalogService catalogService, ProductSearchRepository searchRepository) {
        this.catalogService = catalogService;
        this.searchRepository = searchRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public List<Product> list() {
        return catalogService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public Product get(@PathVariable String id) {
        return catalogService.get(id).orElseThrow();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public List<ProductSearchDocument> search(@RequestParam("q") String q) {
        Map<String, ProductSearchDocument> merged = new LinkedHashMap<>();
        Stream.concat(
                        searchRepository.findByNameContainingIgnoreCase(q).stream(),
                        searchRepository.findByDescriptionContainingIgnoreCase(q).stream())
                .forEach(d -> merged.putIfAbsent(d.getId(), d));
        return merged.values().stream().toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Product create(@RequestBody ProductRequest productRequest) {
        productRequest.setId(null);
        return catalogService.upsert(productRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product update(@PathVariable String id, @RequestBody ProductRequest productRequest) {
        productRequest.setId(id);
        return catalogService.upsert(productRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String id) {
        catalogService.delete(id);
    }
}
