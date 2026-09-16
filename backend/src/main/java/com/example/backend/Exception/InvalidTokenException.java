package com.example.backend.Exception;

/**
 * Custom exception class representing an invalid JWT token.
 */
public class InvalidTokenException extends RuntimeException {
    // Serialization version ID, used for version control during serialization
    private static final long serialVersionUID = 1L;

    /**
     * Creates an InvalidTokenException with the specified error message.
     *
     * @param message the error message describing the exception details
     */
    public InvalidTokenException(String message) {
        super(message); // Call the superclass constructor to set the error message
    }

    /**
     * Creates an InvalidTokenException with the specified error message and root cause.
     *
     * @param message the error message describing the exception details
     * @param cause the original cause of the error, usually another exception
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause); // Call the superclass constructor to set the error message and cause
    }
}
