package com.produit_service.dto.requests;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private int quantite;
}
