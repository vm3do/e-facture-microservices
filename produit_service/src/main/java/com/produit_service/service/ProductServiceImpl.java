package com.produit_service.service;

import com.produit_service.dto.requests.ProductRequest;
import com.produit_service.dto.responses.ProductResponse;
import com.produit_service.entity.Product;
import com.produit_service.exception.InsufficientStockException;
import com.produit_service.exception.ProductNotFoundException;
import com.produit_service.mapper.ProductMapper;
import com.produit_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
    
    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(productMapper::toResponse)
            .toList();
    }
    
    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Produit avec l'ID " + id + " introuvable"));
        return productMapper.toResponse(product);
    }
    
    @Override
    @Transactional
    public ProductResponse decrementStock(Long id, int quantite) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Produit avec l'ID " + id + " non trouvé"));
        
        if (product.getQuantity() < quantite) {
            throw new InsufficientStockException(
                "Stock insuffisant. Disponible: " + product.getQuantity() + ", Demandé: " + quantite
            );
        }
        
        product.setQuantity(product.getQuantity() - quantite);
        Product updatedProduct = productRepository.save(product);
        
        return productMapper.toResponse(updatedProduct);
    }
}
