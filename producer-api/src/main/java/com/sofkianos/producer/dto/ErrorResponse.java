package com.sofkianos.producer.dto;

import java.time.LocalDateTime;

/**
 * Standardized error response body for the Producer API.
 *
 * <p>Used by {@code @RestControllerAdvice} to return a consistent,
 * type-safe JSON error structure. Replacing raw {@code Map<String, Object>}
 * eliminates magic-string keys and enforces immutability.</p>
 *
 * @param timestamp ISO-8601 timestamp of when the error occurred
 * @param status    HTTP status code
 * @param error     human-friendly error summary
 * @param detail    developer-oriented description (never a stack trace)
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String detail
) {

    /**
     * Factory method that auto-populates the timestamp.
     *
     * @param status HTTP status code
     * @param error  human-friendly message
     * @param detail developer-oriented detail
     * @return a new {@link ErrorResponse} with the current timestamp
     */
    public static ErrorResponse of(int status, String error, String detail) {
        return new ErrorResponse(
                LocalDateTime.now().toString(),
                status,
                error,
                detail
        );
    }
}
