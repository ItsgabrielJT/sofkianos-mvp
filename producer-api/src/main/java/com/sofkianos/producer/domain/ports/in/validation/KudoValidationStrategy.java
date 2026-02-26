package com.sofkianos.producer.domain.ports.in.validation;

import com.sofkianos.producer.domain.model.Kudo;

/**
 * Strategy Interface (Input Port) for validating a {@link Kudo} domain model.
 * <p>
 * Multiple implementations can coexist — for example:
 * <ul>
 * <li><strong>SelfKudoValidation</strong> — rejects {@code from == to}</li>
 * <li><strong>CategoryValidation</strong> — ensures the category is a known
 * enum value</li>
 * <li><strong>ProfanityFilterValidation</strong> — scans the message for banned
 * words</li>
 * </ul>
 * Validators are composed at the service layer and executed sequentially,
 * following the <em>Strategy + Chain</em> pattern.
 * </p>
 */
public interface KudoValidationStrategy {
    /**
     * Validates the given domain model.
     * <p>
     * Implementations should throw a domain-specific exception
     * (e.g., {@code InvalidKudoException}) if validation fails.
     * </p>
     *
     * @param kudo the domain model to validate
     */
    void validate(Kudo kudo);
}
