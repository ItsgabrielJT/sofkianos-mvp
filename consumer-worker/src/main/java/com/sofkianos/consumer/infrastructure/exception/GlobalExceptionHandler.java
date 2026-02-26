package com.sofkianos.consumer.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para la capa web del consumer-worker.
 *
 * <p>
 * Intercepta todas las excepciones lanzadas durante el procesamiento de
 * solicitudes HTTP y las convierte en respuestas JSON estandarizadas usando
 * {@link ApiError}. Anotado con {@link RestControllerAdvice} para aplicarse
 * a todos los controladores REST.
 * </p>
 *
 * <p>
 * Cubre los siguientes escenarios:
 * </p>
 * <ul>
 * <li>Fallos de validación de parámetros de entrada ({@code @Valid})</li>
 * <li>Tipo de parámetro incorrecto en la URL o query string</li>
 * <li>Kudo inválido a nivel de dominio ({@link InvalidKudoException})</li>
 * <li>Cualquier excepción de dominio ({@link KudoDomainException})</li>
 * <li>Cualquier excepción no controlada explícitamente</li>
 * </ul>
 *
 * @author Sofkianos
 * @since 1.0
 * @see ApiError
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Maneja fallos de validación de dominio específicos de un {@code Kudo}
     * (ej.: auto-kudo, campos nulos, categoría no permitida).
     *
     * <p>
     * Al ser subclase de {@link KudoDomainException}, este handler tiene
     * precedencia
     * sobre
     * {@link #handleDomainException(KudoDomainException, HttpServletRequest)}.
     * </p>
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [WARN] Invalid Kudo received: sender and recipient cannot be the same user
     * </pre>
     *
     * @param ex      excepción lanzada por una validación de dominio de Kudo
     * @param request solicitud HTTP que originó el error
     * @return {@code 422 Unprocessable Entity} con el detalle del fallo de
     *         validación
     */
    @ExceptionHandler(InvalidKudoException.class)
    public ResponseEntity<ApiError> handleInvalidKudo(InvalidKudoException ex, HttpServletRequest request) {
        log.warn("Invalid Kudo received: {}", ex.getMessage());
        ApiError apiError = ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(apiError);
    }

    /**
     * Maneja cualquier excepción de dominio del bounded context de Kudo que no sea
     * capturada por un handler más específico.
     *
     * <p>
     * Actúa como red de seguridad para toda la jerarquía de
     * {@link KudoDomainException}.
     * </p>
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [WARN] Domain exception: Kudo processing rule violated
     * </pre>
     *
     * @param ex      excepción de dominio no manejada por un handler más específico
     * @param request solicitud HTTP que originó el error
     * @return {@code 422 Unprocessable Entity} con el mensaje de la excepción de
     *         dominio
     */
    @ExceptionHandler(KudoDomainException.class)
    public ResponseEntity<ApiError> handleDomainException(KudoDomainException ex, HttpServletRequest request) {
        log.warn("Domain exception: {}", ex.getMessage());
        ApiError apiError = ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(apiError);
    }

    /**
     * Captura cualquier excepción no manejada explícitamente por los handlers
     * anteriores.
     * Actúa como red de seguridad para evitar que stacktraces internos lleguen al
     * cliente.
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [ERROR] Unexpected error: NullPointerException at KudoListenerService.java:54
     * </pre>
     *
     * @param ex      excepción genérica no controlada
     * @param request solicitud HTTP que originó el error
     * @return {@code 500 Internal Server Error} con un mensaje genérico de error
     *         interno
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError apiError = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
