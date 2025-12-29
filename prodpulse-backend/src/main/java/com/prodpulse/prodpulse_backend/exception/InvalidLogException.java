package com.prodpulse.prodpulse_backend.exception;

/**
 * Exception thrown when log input is invalid
 * (e.g., empty, too long, contains only whitespace)
 */
public class InvalidLogException extends RuntimeException {

    public InvalidLogException(String message) {
        super(message);
    }

    public InvalidLogException(String message, Throwable cause) {
        super(message, cause);
    }
}