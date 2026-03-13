package com.facturationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FactureNotFoundException extends RuntimeException {
    public FactureNotFoundException(String message) {
        super(message);
    }
}
