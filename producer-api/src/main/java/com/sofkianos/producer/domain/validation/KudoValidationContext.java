package com.sofkianos.producer.domain.validation;

import com.sofkianos.producer.domain.ports.in.validation.CategoryAwareValidationStrategy;
import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.exception.InvalidKudoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Validation Context — selects and executes the correct
 * {@link CategoryAwareValidationStrategy} based on the request's category.
 *
 * <p>Spring auto-injects all {@code CategoryAwareValidationStrategy} beans.
 * They are indexed by {@link CategoryAwareValidationStrategy#supportedCategory()}
 * (case-insensitive) at construction time, giving O(1) lookup.</p>
 *
 * <h2>Open/Closed Principle</h2>
 * <p>To add a new category, create a new {@code @Component} implementing
 * {@code CategoryAwareValidationStrategy}. No existing code needs modification.</p>
 */
@Component
public class KudoValidationContext {

    private final Map<String, CategoryAwareValidationStrategy> strategyMap;

    /**
     * Constructs the context from all available strategies.
     *
     * @param strategies all CategoryAwareValidationStrategy beans injected by Spring
     */
    public KudoValidationContext(List<CategoryAwareValidationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.supportedCategory().toLowerCase(),
                        Function.identity()
                ));
    }

    /**
     * Validates the given request using the strategy that matches its category.
     *
     * @param request the incoming kudo request
     * @throws InvalidKudoException if no strategy is registered for the category,
     *                              or if the strategy's validation fails
     */
    public void validate(KudoRequest request) {
        String category = request.getCategory();
        if (category == null || category.isBlank()) {
            throw new InvalidKudoException("Category must not be null or empty");
        }

        CategoryAwareValidationStrategy strategy = strategyMap.get(category.toLowerCase());
        if (strategy == null) {
            throw new InvalidKudoException(
                    String.format("Unsupported category: %s", category));
        }

        strategy.validate(request);
    }
}
