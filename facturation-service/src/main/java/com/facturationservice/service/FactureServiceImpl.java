package com.facturationservice.service;

import com.facturationservice.dto.CreateFactureRequest;
import com.facturationservice.dto.FactureResponse;
import com.facturationservice.dto.LigneFactureRequest;
import com.facturationservice.entity.Facture;
import com.facturationservice.entity.LigneFacture;
import com.facturationservice.feign.ClientFeignClient;
import com.facturationservice.feign.ProductDTO;
import com.facturationservice.feign.ProductFeignClient;
import com.facturationservice.mapper.FactureMapper;
import com.facturationservice.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final ClientFeignClient clientFeignClient;
    private final ProductFeignClient productFeignClient;
    private final FactureMapper factureMapper;

    @Override
    @Transactional
    public FactureResponse createFacture(CreateFactureRequest request) {

        // 1. Vérifier que le client existe (Feign lève FeignException.NotFound → 404)
        clientFeignClient.getClientById(request.getClientId());

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
            ProductDTO product = productFeignClient.getProductById(ligneRequest.getProductId());

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
}


