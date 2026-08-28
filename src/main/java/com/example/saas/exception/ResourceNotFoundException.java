package com.example.saas.exception;

/** Exception métier indépendante du protocole HTTP. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
