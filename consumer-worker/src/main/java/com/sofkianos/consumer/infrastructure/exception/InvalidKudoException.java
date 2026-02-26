package com.sofkianos.consumer.infrastructure.exception;

/**
 * Excepción lanzada cuando un {@code Kudo} recibido falla la validación a nivel
 * de dominio
 * (ej.: campos nulos, intento de auto-kudo, categoría inválida).
 *
 * <p>
 * Extiende {@link KudoDomainException}, por lo que también es capturada por el
 * handler
 * de {@link KudoDomainException} en el {@link GlobalExceptionHandler},
 * mapeándose a
 * <strong>422 Unprocessable Entity</strong>.
 * </p>
 *
 * <p>
 * Ejemplo de log esperado al lanzarse:
 * </p>
 * 
 * <pre>
 * [WARN] Invalid Kudo received: sender and recipient cannot be the same user
 * </pre>
 *
 * @author Sofkianos
 * @since 1.0
 * @see KudoDomainException
 * @see GlobalExceptionHandler#handleInvalidKudo(InvalidKudoException,
 *      jakarta.servlet.http.HttpServletRequest)
 */
public class InvalidKudoException extends KudoDomainException {

    /**
     * Construye la excepción con un mensaje descriptivo del fallo de validación.
     *
     * @param message descripción de la regla de dominio violada
     */
    public InvalidKudoException(String message) {
        super(message);
    }

    /**
     * Construye la excepción con un mensaje descriptivo y la causa original.
     *
     * @param message descripción de la regla de dominio violada
     * @param cause   excepción original que desencadenó este fallo
     */
    public InvalidKudoException(String message, Throwable cause) {
        super(message, cause);
    }
}
