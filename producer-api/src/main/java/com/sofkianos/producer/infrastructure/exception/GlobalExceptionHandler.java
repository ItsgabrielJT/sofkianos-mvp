package com.sofkianos.producer.infrastructure.exception;

import com.sofkianos.producer.domain.exception.KudoNotFoundException;
import com.sofkianos.producer.application.exception.KudoMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global de excepciones para la capa web del producer-api.
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
 * <li>Parámetro requerido ausente en la solicitud</li>
 * <li>Recurso no encontrado en base de datos</li>
 * <li>Fallo al publicar un Kudo al message broker</li>
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
     * Maneja errores de validación disparados por la anotación {@code @Valid}
     * en los cuerpos o parámetros de los endpoints.
     *
     * <p>
     * Recopila todos los errores de campo y los concatena en un único mensaje.
     * </p>
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [WARN] Validation failed: recipientId: must not be null, message: must not be blank
     * </pre>
     *
     * @param ex      excepción de validación de argumentos
     * @param request solicitud HTTP que originó el error
     * @return {@code 400 Bad Request} con el detalle de los campos inválidos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, "Validation failed: " + errors,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Maneja errores cuando un parámetro de la URL o query string tiene un tipo
     * incompatible con el esperado por el método del controlador.
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [WARN] Invalid value for parameter 'page': abc
     * </pre>
     *
     * @param ex      excepción de tipo incorrecto en argumento
     * @param request solicitud HTTP que originó el error
     * @return {@code 400 Bad Request} indicando el parámetro y el valor
     *         problemático
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Maneja errores cuando un parámetro requerido no está presente en la solicitud
     * HTTP.
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [WARN] Missing required parameter: 'employeeId'
     * </pre>
     *
     * @param ex      excepción de parámetro ausente
     * @param request solicitud HTTP que originó el error
     * @return {@code 400 Bad Request} indicando el nombre del parámetro faltante
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String message = "Missing required parameter: '" + ex.getParameterName() + "'";
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Maneja el caso en que se solicita un recurso del dominio que no existe.
     *
     * @param ex      excepción lanzada cuando un recurso de dominio no se encuentra
     * @param request solicitud HTTP que originó el error
     * @return {@code 404 Not Found}
     */
    @ExceptionHandler(KudoNotFoundException.class)
    public ResponseEntity<ApiError> handleKudoNotFound(KudoNotFoundException ex, HttpServletRequest request) {
        log.warn("Domain resource not found: {}", ex.getMessage());
        ApiError apiError = ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    /**
     * Maneja los fallos en la capa de mensajería/aplicación.
     *
     * @param ex      excepción lanzada durante la orquestación del caso de uso
     * @param request solicitud HTTP que originó el error
     * @return {@code 503 Service Unavailable}
     */
    @ExceptionHandler(KudoMessagingException.class)
    public ResponseEntity<ApiError> handleKudoMessaging(KudoMessagingException ex, HttpServletRequest request) {
        log.error("Application messaging failure: {}", ex.getMessage());
        ApiError apiError = ApiError.of(HttpStatus.SERVICE_UNAVAILABLE, "Communication error: " + ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiError);
    }

    /**
     * Maneja fallos al intentar publicar un {@code KudoEvent} al message broker
     * (RabbitMQ), ya sea por error de serialización o por indisponibilidad del
     * broker.
     *
     * <p>
     * Ejemplo de log esperado:
     * </p>
     * 
     * <pre>
     * [ERROR] Failed to publish KudoEvent: Connection refused to RabbitMQ at localhost:5672
     * </pre>
     *
     * @param ex      excepción lanzada durante la publicación del evento de Kudo
     * @param request solicitud HTTP que originó el error
     * @return {@code 503 Service Unavailable} indicando que el broker no está
     *         disponible
     */
    @ExceptionHandler(KudoPublishingException.class)
    public ResponseEntity<ApiError> handleKudoPublishing(KudoPublishingException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiError);
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
     * [ERROR] Internal server error: NullPointerException at KudoServiceImpl.java:87
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
        ApiError apiError = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
