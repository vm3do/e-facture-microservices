package com.facturationservice.service;

import com.facturationservice.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductCircuitBreakerService circuitBreakerService;

    public ProductService(ProductCircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @Retry(name = "produit-service", fallbackMethod = "getProductByIdFallback")
    public ProductDTO getProductById(Long id) {
        log.info("[CALL] Delegating getProductById({}) — will try Retry before CircuitBreaker", id);
        return circuitBreakerService.getProductById(id);
    }

    public ProductDTO getProductByIdFallback(Long id, Throwable t) {
        log.error("[FALLBACK] getProductById({}) — Retries exhausted, returning fallback. Cause: {}", id,
                t != null ? t.getMessage() : "unknown");
        return new ProductDTO(id, "Service Produit Indisponible", 0.0, 0, "FALLBACK");
    }
}

