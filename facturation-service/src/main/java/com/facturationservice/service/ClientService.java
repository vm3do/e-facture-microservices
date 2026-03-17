package com.facturationservice.service;

import com.facturationservice.dto.ClientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientCircuitBreakerService circuitBreakerService;

    public ClientService(ClientCircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @Retry(name = "client-service", fallbackMethod = "getClientByIdFallback")
    public ClientDTO getClientById(Long id) {
        log.info("[CALL] Delegating getClientById({}) — will try Retry before CircuitBreaker", id);
        return circuitBreakerService.getClientById(id);
    }

    public ClientDTO getClientByIdFallback(Long id, Throwable t) {
        log.error("[FALLBACK] getClientById({}) — Retries exhausted, returning fallback. Cause: {}", id,
                t != null ? t.getMessage() : "unknown");
        return new ClientDTO(id, "Service Client Indisponible", "unknown", "FALLBACK");
    }
}

