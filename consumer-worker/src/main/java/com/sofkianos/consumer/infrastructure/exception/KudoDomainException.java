package com.sofkianos.consumer.infrastructure.exception;

/**
 * Excepción base abstracta para el dominio de Kudos.
 *
 * <p>
 * Todas las excepciones específicas del dominio dentro del bounded context de
 * Kudo
 * deben extender esta clase. Esto permite una estrategia unificada de manejo de
 * excepciones en la frontera de la aplicación a través del
 * {@link GlobalExceptionHandler}.
 * </p>
 *
 * <p>
 * Ejemplo de jerarquía:
 * </p>
 * 
 * <pre>
 * KudoDomainException
 * └── InvalidKudoException  (fallo de validación de dominio)
 * </pre>
 *
 * @author Sofkianos
 * @since 1.0
 */
public abstract class KudoDomainException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo del fallo de dominio.
     *
     * @param message descripción del motivo del fallo
     */
    protected KudoDomainException(String message) {
        super(message);
    }

    /**
     * Construye la excepción con un mensaje descriptivo y la causa original.
     *
     * @param message descripción del motivo del fallo
     * @param cause   excepción original que desencadenó este fallo
     */
    protected KudoDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
