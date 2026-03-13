package org.example.testservice.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.example.testservice.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductCircuitBreakerService circuitBreakerService;

    public ProductService(ProductCircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @Retry(name = "produit-service", fallbackMethod = "getAllProductsFallback")
    public List<ProductResponse> getAllProducts() {
        return circuitBreakerService.getAllProducts();
    }

    @Retry(name = "produit-service", fallbackMethod = "getProductByIdFallback")
    public ProductResponse getProductById(Long id) {
        return circuitBreakerService.getProductById(id);
    }


    public List<ProductResponse> getAllProductsFallback(Throwable t) {
        log.error("[FALLBACK] getAllProducts — 3 tentatives epuisees. Cause: {}", t.getMessage());
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
        log.error("[FALLBACK] getProductById({}) — 3 tentatives epuisees. Cause: {}", id, t.getMessage());
        return ProductResponse.builder()
                .id(id)
                .name("Service Produit Indisponible")
                .price(0.0)
                .quantity(0)
                .category("FALLBACK")
                .build();
    }
}
