package com.example.ecommerce.product.repo;

import com.example.ecommerce.product.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
