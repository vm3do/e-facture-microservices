package org.efacture.clientservice.config;

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

    // Use 'productService' label in logs to match messages you requested
    private static final String LOG_LABEL = "productService";
    private static final String RESILIENCE_NAME = "produit-service";

    public ResilienceConfig(RetryRegistry retryRegistry,
                            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerEventListeners() {

        retryRegistry.retry(RESILIENCE_NAME).getEventPublisher()
            .onRetry(event ->
                log.warn("[RETRY] Retry '{}' , attempt #{}",
                    LOG_LABEL,
                    event.getNumberOfRetryAttempts()))

            .onSuccess(event ->
                log.info("[RETRY] Retry '{}' — Success at attempt #{}",
                    LOG_LABEL,
                    event.getNumberOfRetryAttempts()))

            .onError(event ->
                log.error("[RETRY] Retry '{}' — {} attempts exhausted. Circuit Breaker will take over. Cause: {}",
                    LOG_LABEL,
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "unknown"));

        circuitBreakerRegistry.circuitBreaker(RESILIENCE_NAME).getEventPublisher()
            .onStateTransition(event -> {
                CircuitBreaker.StateTransition transition = event.getStateTransition();
                if (transition.getToState() == CircuitBreaker.State.OPEN) {
                    log.error("[CIRCUIT BREAKER] Circuit Breaker '{}' → OPEN (too many failures)", LOG_LABEL);
                } else if (transition.getToState() == CircuitBreaker.State.HALF_OPEN) {
                    log.warn("[CIRCUIT BREAKER] Circuit Breaker '{}' → HALF_OPEN (recovery test)", LOG_LABEL);
                } else if (transition.getToState() == CircuitBreaker.State.CLOSED) {
                    log.info("[CIRCUIT BREAKER] Circuit Breaker '{}' → CLOSED (service recovered)", LOG_LABEL);
                }
            })
            .onCallNotPermitted(event ->
                log.error("[CIRCUIT BREAKER] Call blocked — Circuit Breaker '{}' is OPEN", LOG_LABEL))
            .onError(event ->
                log.error("[CIRCUIT BREAKER] Failure recorded — duration: {}ms, cause: {}",
                    event.getElapsedDuration().toMillis(),
                    event.getThrowable() != null ? event.getThrowable().getMessage() : "unknown"));
    }
}

