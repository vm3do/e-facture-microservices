package com.produit_service.dto.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private double price;
    private int quantity;
    private String category;
}
