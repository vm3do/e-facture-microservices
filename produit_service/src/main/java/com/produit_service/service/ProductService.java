package com.produit_service.service;

import com.produit_service.dto.requests.ProductRequest;
import com.produit_service.dto.responses.ProductResponse;
import com.produit_service.entity.Product;
import com.produit_service.exception.ProductNotFoundException;
import com.produit_service.mapper.ProductMapper;
import com.produit_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
    
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(productMapper::toResponse)
            .toList();
    }
    
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Produit avec l'ID " + id + " introuvable"));
        return productMapper.toResponse(product);
    }
}
