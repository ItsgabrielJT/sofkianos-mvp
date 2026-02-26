package com.sofkianos.producer.infrastructure.exception;

/**
 * Excepción lanzada cuando la publicación de un {@code KudoEvent}
 * al message broker falla (ej.: error de serialización, broker no disponible).
 *
 * <p>
 * Es capturada por el {@link GlobalExceptionHandler} y mapeada a
 * <strong>503 Service Unavailable</strong>.
 * </p>
 *
 * <p>
 * Ejemplo de log esperado al lanzarse:
 * </p>
 * 
 * <pre>
 * [ERROR] Failed to publish KudoEvent to exchange 'kudo.exchange': Connection refused
 * </pre>
 *
 * @author Sofkianos
 * @since 1.0
 * @see GlobalExceptionHandler#handleKudoPublishing(KudoPublishingException,
 *      jakarta.servlet.http.HttpServletRequest)
 */
public class KudoPublishingException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo del fallo.
     *
     * @param message descripción del motivo del fallo en la publicación
     */
    public KudoPublishingException(String message) {
        super(message);
    }

    /**
     * Construye la excepción con un mensaje descriptivo y la causa original.
     *
     * @param message descripción del motivo del fallo en la publicación
     * @param cause   excepción original que desencadenó este fallo
     */
    public KudoPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
