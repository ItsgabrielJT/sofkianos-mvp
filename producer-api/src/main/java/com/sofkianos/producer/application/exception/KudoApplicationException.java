package com.sofkianos.producer.application.exception;

/**
 * Clase base para excepciones de la capa de aplicación.
 */
public abstract class KudoApplicationException extends RuntimeException {
    protected KudoApplicationException(String message) {
        super(message);
    }

    protected KudoApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
