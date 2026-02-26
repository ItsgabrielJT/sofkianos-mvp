package com.sofkianos.consumer.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Estructura de error estandarizada para todas las respuestas de error del
 * consumer-worker.
 *
 * <p>
 * Serializada como cuerpo JSON en todas las respuestas de error producidas por
 * el {@link GlobalExceptionHandler}. El timestamp se formatea como
 * {@code yyyy-MM-dd'T'HH:mm:ss} para consistencia entre servicios.
 * </p>
 *
 * <p>
 * Uso típico:
 * </p>
 * 
 * <pre>{@code
 * ApiError error = ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, "Self-kudo not allowed", "/api/kudos");
 * }</pre>
 *
 * @author Sofkianos
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class ApiError {

    /**
     * Momento exacto en que ocurrió el error, formateado como
     * {@code yyyy-MM-dd'T'HH:mm:ss}.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Código numérico HTTP de la respuesta de error (ej.: 400, 422, 500).
     */
    private int status;

    /**
     * Razón textual del estado HTTP (ej.: "Bad Request", "Unprocessable Entity").
     */
    private String error;

    /**
     * Mensaje descriptivo del error, legible por el consumidor de la API.
     */
    private String message;

    /**
     * Ruta de la solicitud HTTP que provocó el error (ej.: {@code /api/kudos}).
     */
    private String path;

    /**
     * Méthod de fábrica estático para construir un {@code ApiError} de forma
     * conveniente.
     *
     * @param status  el {@link HttpStatus} que representa el tipo de error
     * @param message descripción del error para el consumidor
     * @param path    URI de la solicitud que originó el error
     * @return una instancia de {@code ApiError} con timestamp generado al momento
     *         de la llamada
     */
    public static ApiError of(HttpStatus status, String message, String path) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
