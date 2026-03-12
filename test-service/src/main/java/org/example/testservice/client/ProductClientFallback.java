package org.example.testservice.client;

import org.example.testservice.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public List<ProductResponse> getAllProducts() {
        log.warn("[FALLBACK] produit-service indisponible → getAllProducts()");
        return Collections.emptyList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        log.warn("[FALLBACK] produit-service indisponible → getProductById({})", id);
        return ProductResponse.builder()
                .id(id)
                .name("SERVICE INDISPONIBLE")
                .price(0.0)
                .quantity(0)
                .category("N/A")
                .build();
    }
}

