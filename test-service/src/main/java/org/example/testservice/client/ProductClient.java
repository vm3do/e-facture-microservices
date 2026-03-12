package org.example.testservice.client;

import org.example.testservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "produit-service", url = "http://localhost:8082")
public interface ProductClient {

    @GetMapping("/api/produits")
    List<ProductResponse> getAllProducts();

    @GetMapping("/api/produits/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
}
