package org.efacture.clientservice.mapper;

import org.efacture.clientservice.dto.ClientResponse;
import org.efacture.clientservice.dto.CreateClientRequest;
import org.efacture.clientservice.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client toEntity(CreateClientRequest request);

    ClientResponse toResponse(Client client);
}
