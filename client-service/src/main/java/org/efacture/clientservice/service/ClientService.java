package org.efacture.clientservice.service;

import org.efacture.clientservice.dto.ClientResponse;
import org.efacture.clientservice.dto.CreateClientRequest;

import java.util.List;

public interface ClientService {

    ClientResponse createClient(CreateClientRequest request);
    List<ClientResponse> getAllClients();
    ClientResponse getClientById(Long id);
    void deleteClient(Long id);
}
