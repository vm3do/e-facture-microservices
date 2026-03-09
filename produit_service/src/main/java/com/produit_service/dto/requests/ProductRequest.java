package com.produit_service.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;
    
    @Positive(message = "Le prix doit être supérieur à 0")
    private double price;
    
    @Min(value = 0, message = "Le stock doit être supérieur ou égal à 0")
    private int quantity;
    
    @NotBlank(message = "La catégorie est obligatoire")
    private String category;
}
