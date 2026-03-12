package com.facturationservice.feign;

import com.facturationservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "produit-service")
public interface ProductFeign {

    @GetMapping("/api/produits/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}

