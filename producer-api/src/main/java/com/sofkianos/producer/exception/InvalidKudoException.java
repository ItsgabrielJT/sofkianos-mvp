package com.sofkianos.producer.exception;

/**
 * Thrown when a {@link com.sofkianos.producer.dto.KudoRequest} fails
 * domain-level validation executed by a
 * {@link com.sofkianos.producer.domain.ports.in.validation.KudoValidationStrategy}.
 *
 * <p>Caught by the {@code GlobalExceptionHandler} and mapped to
 * <strong>400 Bad Request</strong>.</p>
 */
public class InvalidKudoException extends RuntimeException {

    public InvalidKudoException(String message) {
        super(message);
    }

    public InvalidKudoException(String message, Throwable cause) {
        super(message, cause);
    }
}
