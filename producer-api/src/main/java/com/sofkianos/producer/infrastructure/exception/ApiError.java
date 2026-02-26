package com.sofkianos.producer.infrastructure.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

/**
 * Estructura de error estandarizada para todas las respuestas de error de la
 * API.
 *
 * <p>
 * Este objeto es serializado como cuerpo JSON en todas las respuestas de error
 * producidas por el {@link GlobalExceptionHandler}. Incluye un timestamp con
 * zona
 * horaria ({@link OffsetDateTime}), el código HTTP numérico, la razón HTTP
 * textual,
 * un mensaje descriptivo y la ruta de la solicitud que originó el error.
 * </p>
 *
 * <p>
 * Uso típico:
 * </p>
 * 
 * <pre>{@code
 * ApiError error = ApiError.of(HttpStatus.NOT_FOUND, "Recurso no encontrado", "/api/kudos/99");
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
     * Momento exacto en que ocurrió el error, con zona horaria (UTC offset).
     * Se serializa automáticamente en formato ISO-8601.
     */
    private OffsetDateTime timestamp;

    /**
     * Código numérico HTTP de la respuesta de error (ej.: 400, 404, 500).
     */
    private int status;

    /**
     * Razón textual del estado HTTP (ej.: "Bad Request", "Not Found").
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
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
