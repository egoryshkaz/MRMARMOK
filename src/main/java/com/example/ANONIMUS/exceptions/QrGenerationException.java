package com.example.ANONIMUS.exceptions;

public class QrGenerationException extends RuntimeException {
    public QrGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}