package org.example.testservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.testservice.client.ProductClient;
import org.example.testservice.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductClient productClient;

    public ProductService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "produit-service", fallbackMethod = "getAllProductsFallback")
    public List<ProductResponse> getAllProducts() {
        return productClient.getAllProducts();
    }

    @CircuitBreaker(name = "produit-service", fallbackMethod = "getProductByIdFallback")
    public ProductResponse getProductById(Long id) {
        return productClient.getProductById(id);
    }

    public List<ProductResponse> getAllProductsFallback(Throwable t) {
        log.error("[CIRCUIT BREAKER] getAllProducts fallback. Cause: {}", t.getMessage());
        return Collections.singletonList(
                ProductResponse.builder()
                        .id(-1L)
                        .name("Service Produit Indisponible")
                        .price(0.0)
                        .quantity(0)
                        .category("FALLBACK")
                        .build()
        );
    }

    public ProductResponse getProductByIdFallback(Long id, Throwable t) {
        log.error("[CIRCUIT BREAKER] getProductById({}) fallback. Cause: {}", id, t.getMessage());
        return ProductResponse.builder()
                .id(id)
                .name("Service Produit Indisponible")
                .price(0.0)
                .quantity(0)
                .category("FALLBACK")
                .build();
    }
}

