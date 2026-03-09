package com.produit_service.controller;

import com.produit_service.dto.requests.ProductRequest;
import com.produit_service.dto.requests.StockUpdateRequest;
import com.produit_service.dto.responses.ProductResponse;
import com.produit_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }
    
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }
    
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
    
    @PatchMapping("/{id}/stock")
    public ProductResponse decrementStock(
        @PathVariable Long id,
        @Valid @RequestBody StockUpdateRequest request
    ) {
        return productService.decrementStock(id, request.getQuantite());
    }
}
