package com.produit_service.service;

import com.produit_service.dto.requests.ProductRequest;
import com.produit_service.dto.responses.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse decrementStock(Long id, int quantite);
}
