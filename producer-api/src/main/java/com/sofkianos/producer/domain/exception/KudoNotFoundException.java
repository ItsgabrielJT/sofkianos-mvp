package com.sofkianos.producer.domain.exception;

/**
 * Excepción lanzada cuando un recurso de dominio no existe.
 */
public class KudoNotFoundException extends DomainException {
    public KudoNotFoundException(String message) {
        super(message);
    }
}
