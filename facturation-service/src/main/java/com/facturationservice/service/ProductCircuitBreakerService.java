// New file: ProductCircuitBreakerService
package com.facturationservice.service;

import com.facturationservice.dto.ProductDTO;
import com.facturationservice.feign.ProductFeign;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class ProductCircuitBreakerService {

    private final ProductFeign productFeign;

    public ProductCircuitBreakerService(ProductFeign productFeign) {
        this.productFeign = productFeign;
    }

    @CircuitBreaker(name = "produit-service")
    public ProductDTO getProductById(Long id) {
        return productFeign.getProductById(id);
    }
}
