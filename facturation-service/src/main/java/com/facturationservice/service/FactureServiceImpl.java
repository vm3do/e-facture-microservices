package com.facturationservice.service;

import com.facturationservice.dto.*;
import com.facturationservice.entity.Facture;
import com.facturationservice.entity.LigneFacture;
import com.facturationservice.exception.FactureNotFoundException;
import com.facturationservice.service.ClientService;
import com.facturationservice.service.ProductService;
import com.facturationservice.mapper.FactureMapper;
import com.facturationservice.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final ClientService clientService;
    private final ProductService productService;
    private final FactureMapper factureMapper;

    @Override
    @Transactional
    public FactureResponse createFacture(CreateFactureRequest request) {

        // 1. Vérifier que le client existe (Feign lève FeignException.NotFound → 404)
        clientService.getClientById(request.getClientId());

        // 2. Construire la facture
        Facture facture = Facture.builder()
                .billingDate(LocalDate.now())
                .clientId(request.getClientId())
                .lignes(new ArrayList<>())
                .build();

        // 3. Pour chaque ligne : vérifier le produit et capturer le prix
        List<LigneFacture> lignes = new ArrayList<>();
        for (LigneFactureRequest ligneRequest : request.getLignes()) {

            // Feign lève FeignException.NotFound si le produit n'existe pas
            ProductDTO product = productService.getProductById(ligneRequest.getProductId());

            LigneFacture ligne = LigneFacture.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())   // prix capturé au moment de la création
                    .quantity(ligneRequest.getQuantity())
                    .facture(facture)
                    .build();

            lignes.add(ligne);
        }

        facture.setLignes(lignes);

        Facture savedFacture = factureRepository.save(facture);
        return factureMapper.toFactureResponse(savedFacture);
    }

    @Override
    @Transactional(readOnly = true)
    public FactureEnrichedResponse getFactureById(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new FactureNotFoundException("Facture non trouvée avec l'ID : " + id));

        ClientDTO client = clientService.getClientById(facture.getClientId());

        List<LigneFactureEnrichedResponse> lignesEnriched = facture.getLignes().stream()
                .map(ligne -> {
                    ProductDTO product = productService.getProductById(ligne.getProductId());
                    return LigneFactureEnrichedResponse.builder()
                            .id(ligne.getId())
                            .product(product)
                            .quantity(ligne.getQuantity())
                            .unitPrice(ligne.getUnitPrice())
                            .subtotal(ligne.getQuantity() * ligne.getUnitPrice())
                            .build();
                })
                .collect(Collectors.toList());

        double totalAmount = lignesEnriched.stream()
                .mapToDouble(LigneFactureEnrichedResponse::getSubtotal)
                .sum();

        return FactureEnrichedResponse.builder()
                .id(facture.getId())
                .billingDate(facture.getBillingDate())
                .client(client)
                .lignes(lignesEnriched)
                .totalAmount(totalAmount)
                .build();
    }
}
