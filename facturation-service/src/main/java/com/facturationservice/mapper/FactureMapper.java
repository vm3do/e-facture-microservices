package com.facturationservice.mapper;

import com.facturationservice.dto.FactureEnrichedResponse;
import com.facturationservice.dto.FactureResponse;
import com.facturationservice.dto.LigneFactureEnrichedResponse;
import com.facturationservice.dto.LigneFactureResponse;
import com.facturationservice.entity.Facture;
import com.facturationservice.entity.LigneFacture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface FactureMapper {

    FactureResponse toFactureResponse(Facture facture);

    LigneFactureResponse toLigneFactureResponse(LigneFacture ligneFacture);

    @Mappings({
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "lignes", ignore = true),
            @Mapping(target = "totalAmount", ignore = true)
    })
    FactureEnrichedResponse toFactureEnrichedResponse(Facture facture);

    @Mappings({
            @Mapping(target = "product", ignore = true),
            @Mapping(target = "subtotal", ignore = true)
    })
    LigneFactureEnrichedResponse toLigneFactureEnrichedResponse(LigneFacture ligneFacture);
}
