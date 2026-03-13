package org.example.testservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResilienceConfig(RetryRegistry retryRegistry,
                            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerEventListeners() {

        // ─── Retry Event Listeners ────────────────────────────────────────────
        retryRegistry.retry("produit-service").getEventPublisher()

            .onRetry(event ->
                log.warn("[RETRY] Retry 'produit-service', attempt #{} — Cause: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable() != null
                        ? event.getLastThrowable().getMessage()
                        : "unknown"))

            .onSuccess(event ->
                log.info("[RETRY] Retry 'produit-service' — Succès à la tentative #{}",
                    event.getNumberOfRetryAttempts()))

            .onError(event ->
                log.error("[RETRY] Retry 'produit-service' — {} tentatives épuisées. Le Circuit Breaker prend le relais. Cause: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable() != null
                        ? event.getLastThrowable().getMessage()
                        : "unknown"));

        // ─── Circuit Breaker Event Listeners ─────────────────────────────────
        circuitBreakerRegistry.circuitBreaker("produit-service").getEventPublisher()

            .onStateTransition(event -> {
                CircuitBreaker.StateTransition transition = event.getStateTransition();
                if (transition.getToState() == CircuitBreaker.State.OPEN) {
                    log.error("[CIRCUIT BREAKER] Circuit Breaker 'produit-service' → OPEN (trop d'échecs détectés)");
                } else if (transition.getToState() == CircuitBreaker.State.HALF_OPEN) {
                    log.warn("[CIRCUIT BREAKER] Circuit Breaker 'produit-service' → HALF_OPEN (test de récupération)");
                } else if (transition.getToState() == CircuitBreaker.State.CLOSED) {
                    log.info("[CIRCUIT BREAKER] Circuit Breaker 'produit-service' → CLOSED (service rétabli)");
                }
            })

            .onCallNotPermitted(event ->
                log.error("[CIRCUIT BREAKER] Appel bloqué — Circuit Breaker 'produit-service' est OPEN"))

            .onError(event ->
                log.error("[CIRCUIT BREAKER] Échec enregistré — durée: {}ms, cause: {}",
                    event.getElapsedDuration().toMillis(),
                    event.getThrowable().getMessage()));
    }
}

