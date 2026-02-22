package com.sofkianos.producer.domain.validation;

import com.sofkianos.producer.domain.ports.in.validation.CategoryAwareValidationStrategy;
import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.exception.InvalidKudoException;
import org.springframework.stereotype.Component;

/**
 * Strategy for <strong>Mastery</strong> kudos.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Self-kudo protection: {@code from} must differ from {@code to}</li>
 *   <li>Message minimum length: 25 characters (mastery kudos should detail the expertise demonstrated)</li>
 * </ul>
 */
@Component
public class MasteryValidationStrategy implements CategoryAwareValidationStrategy {

    private static final int MIN_MESSAGE_LENGTH = 25;

    @Override
    public String supportedCategory() {
        return "Mastery";
    }

    @Override
    public void validate(KudoRequest request) {
        rejectSelfKudo(request);

        if (request.getMessage() != null && request.getMessage().trim().length() < MIN_MESSAGE_LENGTH) {
            throw new InvalidKudoException(
                    String.format("Mastery kudos require a message of at least %d characters", MIN_MESSAGE_LENGTH));
        }
    }

    private void rejectSelfKudo(KudoRequest request) {
        if (request.getFrom() != null && request.getTo() != null
                && request.getFrom().equalsIgnoreCase(request.getTo())) {
            throw new InvalidKudoException("Cannot send a kudo to yourself");
        }
    }
}
