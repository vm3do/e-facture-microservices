package org.example.testservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.testservice.client.ProductClient;
import org.example.testservice.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductCircuitBreakerService {

    private final ProductClient productClient;

    public ProductCircuitBreakerService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "produit-service")
    public List<ProductResponse> getAllProducts() {
        return productClient.getAllProducts();
    }

    @CircuitBreaker(name = "produit-service")
    public ProductResponse getProductById(Long id) {
        return productClient.getProductById(id);
    }
}
