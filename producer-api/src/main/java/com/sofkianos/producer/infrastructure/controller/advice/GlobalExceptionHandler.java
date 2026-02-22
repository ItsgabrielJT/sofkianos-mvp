package com.sofkianos.producer.infrastructure.controller.advice;

import com.sofkianos.producer.dto.ErrorResponse;
import com.sofkianos.producer.exception.InvalidKudoException;
import com.sofkianos.producer.exception.KudoPublishingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Global exception handler for the Producer API.
 * <p>
 * Translates domain and infrastructure exceptions into
 * well-structured HTTP error responses.
 * </p>
 *
 * <h2>Error response shape</h2>
 * <p>Handlers return a JSON object with the following keys:</p>
 * <ul>
 *   <li>{@code timestamp} (ISO-8601 string)</li>
 *   <li>{@code status} (HTTP status code)</li>
 *   <li>{@code error} (high-level message)</li>
 *   <li>{@code detail} (developer-oriented detail)</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * {
 *   "timestamp": "2026-02-19T10:12:33.123",
 *   "status": 503,
 *   "error": "The messaging service is temporarily unavailable. Please try again later.",
 *   "detail": "Error publishing KudoEvent to message broker"
 * }
 * }</pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 Bad Request — Domain validation (Strategy) failure ────────
    /**
     * Converts {@link InvalidKudoException} — thrown by Strategy validation —
     * into an HTTP {@code 400 Bad Request} response.
     *
     * @param ex the domain validation exception
     * @return a structured error body with the validation failure detail
     */
    @ExceptionHandler(InvalidKudoException.class)
    public ResponseEntity<ErrorResponse> handleInvalidKudoException(
            InvalidKudoException ex) {

        log.warn("Domain validation failed: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Domain validation failed",
                        ex.getMessage()
                ));
    }

    // ── 503 Service Unavailable — messaging infrastructure failure ──────
        /**
         * Converts {@link KudoPublishingException} into an HTTP {@code 503 Service Unavailable} response.
         *
         * @param ex the publishing exception
         * @return a structured error body suitable for clients
         */
    @ExceptionHandler(KudoPublishingException.class)
    public ResponseEntity<ErrorResponse> handleKudoPublishingException(
            KudoPublishingException ex) {

        log.error("Messaging failure: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "The messaging service is temporarily unavailable. Please try again later.",
                        ex.getMessage()
                ));
    }

    // ── 400 Bad Request — Bean Validation failures ──────────────────────
        /**
         * Converts Bean Validation errors into an HTTP {@code 400 Bad Request} response.
         *
         * @param ex validation exception thrown by Spring MVC when {@code @Valid} fails
         * @return a structured error body with a field-level summary in {@code detail}
         */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                ));
    }

    // ── 400 Bad Request — Method parameter validation (@Validated) ────
        /**
         * Handles constraint violations on {@code @RequestParam} and other
         * method parameters annotated with Jakarta Validation constraints.
         *
         * @param ex exception thrown by Spring when {@code @Validated} controller
         *           method parameters fail validation
         * @return a {@code 400 Bad Request} response with validation details
         */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(
            HandlerMethodValidationException ex) {

        log.warn("Method parameter validation failed: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        ex.getMessage()
                ));
    }

    // ── 404 Not Found — static resource requests (favicon, /, etc.) ────
        /**
         * Handles static resource misses when static mappings are disabled.
         *
         * @param ex exception raised by Spring MVC for missing resources
         * @return a {@code 404 Not Found} response
         */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex) {

        log.debug("Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Resource not found",
                        ex.getMessage()
                ));
    }

    // ── 500 Internal Server Error — catch-all ───────────────────────────
        /**
         * Catch-all handler that prevents stack traces from leaking to clients.
         *
         * @param ex unexpected exception
         * @return a {@code 500 Internal Server Error} response
         */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred. Please contact support.",
                        ex.getMessage()
                ));
    }
}
