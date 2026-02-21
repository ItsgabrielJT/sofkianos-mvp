package com.sofkianos.producer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Query parameter binding object for the public Kudo listing endpoint.
 *
 * <p>Provides default values and validation constraints for pagination
 * and sorting parameters. Uses class (not record) because Spring MVC's
 * {@code @ModelAttribute} binding requires setter-based property access
 * for proper default value handling.</p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code page}: non-negative integer (default 0)</li>
 *   <li>{@code size}: 1–50 inclusive (default 20)</li>
 *   <li>{@code sortDirection}: strictly "ASC" or "DESC" (default "DESC")</li>
 * </ul>
 */
@Getter
@Setter
public class KudoListRequest {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 50;
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    private static final String SORT_DIRECTION_PATTERN = "^(ASC|DESC)$";

    @Min(value = DEFAULT_PAGE, message = "page debe ser mayor o igual a 0")
    private int page = DEFAULT_PAGE;

    @Min(value = MIN_SIZE, message = "size debe estar entre 1 y 50")
    @Max(value = MAX_SIZE, message = "size debe estar entre 1 y 50")
    private int size = DEFAULT_SIZE;

    @Pattern(regexp = SORT_DIRECTION_PATTERN, message = "sortDirection debe ser ASC o DESC")
    private String sortDirection = DEFAULT_SORT_DIRECTION;
}
