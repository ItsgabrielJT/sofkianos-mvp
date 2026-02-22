package com.sofkianos.producer.domain.validation;

import com.sofkianos.producer.domain.ports.in.validation.CategoryAwareValidationStrategy;
import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.exception.InvalidKudoException;
import org.springframework.stereotype.Component;

/**
 * Strategy for <strong>Innovation</strong> kudos.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Self-kudo protection: {@code from} must differ from {@code to}</li>
 *   <li>Message minimum length: 20 characters (innovation kudos should describe the idea)</li>
 * </ul>
 */
@Component
public class InnovationValidationStrategy implements CategoryAwareValidationStrategy {

    private static final int MIN_MESSAGE_LENGTH = 20;

    @Override
    public String supportedCategory() {
        return "Innovation";
    }

    @Override
    public void validate(KudoRequest request) {
        rejectSelfKudo(request);

        if (request.getMessage() != null && request.getMessage().trim().length() < MIN_MESSAGE_LENGTH) {
            throw new InvalidKudoException(
                    String.format("Innovation kudos require a message of at least %d characters", MIN_MESSAGE_LENGTH));
        }
    }

    private void rejectSelfKudo(KudoRequest request) {
        if (request.getFrom() != null && request.getTo() != null
                && request.getFrom().equalsIgnoreCase(request.getTo())) {
            throw new InvalidKudoException("Cannot send a kudo to yourself");
        }
    }
}
