package com.example.saas.exception;

/** Erreur dédiée à un tenant absent ou mal formé. */
public class InvalidTenantException extends RuntimeException {
    public InvalidTenantException(String message) {
        super(message);
    }
}
