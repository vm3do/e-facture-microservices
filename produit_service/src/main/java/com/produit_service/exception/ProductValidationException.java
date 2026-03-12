package com.produit_service.exception;

public class ProductValidationException extends RuntimeException {
    public ProductValidationException(String message) {
        super(message);
    }
}
