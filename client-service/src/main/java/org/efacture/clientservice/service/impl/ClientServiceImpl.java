package org.efacture.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.efacture.clientservice.dto.ClientResponse;
import org.efacture.clientservice.dto.CreateClientRequest;
import org.efacture.clientservice.entity.Client;
import org.efacture.clientservice.exception.ClientAlreadyExistsException;
import org.efacture.clientservice.exception.NotFoundRessourceException;
import org.efacture.clientservice.mapper.ClientMapper;
import org.efacture.clientservice.repository.ClientRepository;
import org.efacture.clientservice.service.ClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse createClient(CreateClientRequest request) {
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new ClientAlreadyExistsException(
                    "A client with email '" + request.getEmail() + "' already exists");
        }

        Client client = clientMapper.toEntity(request);
        Client savedClient = clientRepository.save(client);

        return clientMapper.toResponse(savedClient);
    }

    @Override
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Override
    public ClientResponse getClientById(Long id) {
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new NotFoundRessourceException("Client not found with id: " + id));

        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public void deleteClient(Long id){
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundRessourceException("Client not found"));
        clientRepository.deleteById(id);
    }
}
