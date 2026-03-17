package com.facturationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFactureResponse {

    private Long id;
    private Long productId;
    private String productName;
    private double unitPrice;
    private int quantity;
}

