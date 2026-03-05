package com.produit_service.mapper;

import com.produit_service.dto.requests.ProductRequest;
import com.produit_service.dto.responses.ProductResponse;
import com.produit_service.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);
    Product toEntity(ProductRequest request);
}
