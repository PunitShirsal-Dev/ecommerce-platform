package com.example.ecommerce.product.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, String> {

    List<ProductSearchDocument> findByNameContainingIgnoreCase(String name);

    List<ProductSearchDocument> findByDescriptionContainingIgnoreCase(String description);
}
