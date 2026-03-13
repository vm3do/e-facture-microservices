package com.facturationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureResponse {

    private Long id;
    private LocalDate billingDate;
    private Long clientId;
    private List<LigneFactureResponse> lignes;
}

