package org.example.testservice.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.testservice.dto.ProductResponse;
import org.example.testservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final ProductService productService;

    public TestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/produits")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/produits/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}

