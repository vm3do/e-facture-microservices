package com.facturationservice.controller;

import com.facturationservice.dto.CreateFactureRequest;
import com.facturationservice.dto.FactureResponse;
import com.facturationservice.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    /**
     * POST /api/factures
     * Crée une facture pour un client existant avec une ou plusieurs lignes produit.
     * Le prix est capturé au moment de la création via Feign.
     * Retourne 201 Created.
     */
    @PostMapping
    public ResponseEntity<FactureResponse> createFacture(@Valid @RequestBody CreateFactureRequest request) {
        FactureResponse response = factureService.createFacture(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

