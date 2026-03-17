package com.facturationservice.config;

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

    // The resilience instance names used in Feign clients and configuration
    private static final String PRODUIT_NAME = "produit-service";
    private static final String CLIENT_NAME = "client-service";

    public ResilienceConfig(RetryRegistry retryRegistry,
                            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerEventListeners() {
        // Register for produit-service (logged as productService)
        registerRetryListener(PRODUIT_NAME, "productService");
        registerCircuitBreakerListener(PRODUIT_NAME, "productService");

        // Register for client-service (logged as clientService)
        registerRetryListener(CLIENT_NAME, "clientService");
        registerCircuitBreakerListener(CLIENT_NAME, "clientService");
    }

    private void registerRetryListener(String instanceName, String logLabel) {
        retryRegistry.retry(instanceName).getEventPublisher()
            .onRetry(event ->
                log.warn("Retry '{}' , attempt {}", logLabel, event.getNumberOfRetryAttempts()))
            .onSuccess(event ->
                log.info("Retry '{}' — Success at attempt {}", logLabel, event.getNumberOfRetryAttempts()))
            .onError(event ->
                log.error("Retry '{}' — {} attempts exhausted. Circuit Breaker will take over. Cause: {}",
                    logLabel,
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "unknown"));
    }

    private void registerCircuitBreakerListener(String instanceName, String logLabel) {
        circuitBreakerRegistry.circuitBreaker(instanceName).getEventPublisher()
            .onStateTransition(event -> {
                CircuitBreaker.StateTransition transition = event.getStateTransition();
                if (transition.getToState() == CircuitBreaker.State.OPEN) {
                    log.error("CircuitBreaker '{}' opened", logLabel);
                } else if (transition.getToState() == CircuitBreaker.State.HALF_OPEN) {
                    log.warn("CircuitBreaker '{}' half-open (recovery test)", logLabel);
                } else if (transition.getToState() == CircuitBreaker.State.CLOSED) {
                    log.info("CircuitBreaker '{}' closed (service recovered)", logLabel);
                }
            })
            .onCallNotPermitted(event ->
                log.error("Call blocked — CircuitBreaker '{}' is OPEN", logLabel))
            .onError(event ->
                log.error("[CIRCUIT BREAKER] Failure recorded for '{}' — duration: {}ms, cause: {}",
                    logLabel,
                    event.getElapsedDuration().toMillis(),
                    event.getThrowable() != null ? event.getThrowable().getMessage() : "unknown"));
    }
}
