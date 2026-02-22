package com.sofkianos.producer.domain.ports.in.validation;

/**
 * Marker method for category-aware strategies.
 *
 * <p>Each {@link KudoValidationStrategy} implementation declares which
 * category it supports, allowing {@code KudoValidationContext} to route
 * requests dynamically without if/else chains.</p>
 */
public interface CategoryAwareValidationStrategy extends KudoValidationStrategy {

    /**
     * Returns the category this strategy handles (case-insensitive).
     *
     * @return the supported category name (e.g., "Innovation", "Teamwork")
     */
    String supportedCategory();
}
