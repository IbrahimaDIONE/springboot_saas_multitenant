package com.example.saas.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String m) {
        super(m);
    }
}
