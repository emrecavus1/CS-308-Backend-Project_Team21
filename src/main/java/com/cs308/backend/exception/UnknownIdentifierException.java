package com.cs308.backend.exception;

public class UnknownIdentifierException extends RuntimeException {
    public UnknownIdentifierException(String message) {
        super(message);
    }
}
