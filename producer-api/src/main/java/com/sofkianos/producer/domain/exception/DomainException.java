package com.sofkianos.producer.domain.exception;

/**
 * Clase base para todas las excepciones del dominio.
 * Totalmente independiente de frameworks de infraestructura.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
