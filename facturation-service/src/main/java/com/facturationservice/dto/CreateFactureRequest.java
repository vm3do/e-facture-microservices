package com.facturationservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateFactureRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;

    @NotEmpty(message = "La facture doit contenir au moins une ligne produit")
    @Valid
    private List<LigneFactureRequest> lignes;
}

