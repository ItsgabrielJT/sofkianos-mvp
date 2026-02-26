package com.sofkianos.producer.application.exception;

/**
 * Excepción lanzada cuando hay un error en la comunicación con servicios
 * externos
 * (ej. RabbitMQ) durante la ejecución de un caso de uso.
 */
public class KudoMessagingException extends KudoApplicationException {
    public KudoMessagingException(String message) {
        super(message);
    }

    public KudoMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
