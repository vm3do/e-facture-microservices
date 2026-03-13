package com.facturationservice.service;

import com.facturationservice.dto.ClientDTO;
import com.facturationservice.feign.ClientFeign;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class ClientCircuitBreakerService {

    private final ClientFeign clientFeign;

    public ClientCircuitBreakerService(ClientFeign clientFeign) {
        this.clientFeign = clientFeign;
    }

    @CircuitBreaker(name = "client-service")
    public ClientDTO getClientById(Long id) {
        return clientFeign.getClientById(id);
    }
}

