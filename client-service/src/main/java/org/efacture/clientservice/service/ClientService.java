package org.efacture.clientservice.service;

import org.efacture.clientservice.dto.ClientResponse;
import org.efacture.clientservice.dto.CreateClientRequest;

public interface ClientService {

    ClientResponse createClient(CreateClientRequest request);
}
