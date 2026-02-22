package com.sofkianos.producer.domain.validation;

import com.sofkianos.producer.domain.ports.in.validation.CategoryAwareValidationStrategy;
import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.exception.InvalidKudoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Unit tests for {@link KudoValidationContext}.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>TC-R12-001 — Resolves and executes strategy by category</li>
 *   <li>TC-R12-002 — Unregistered category throws InvalidKudoException</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class KudoValidationContextTest {

    @Mock
    private CategoryAwareValidationStrategy innovationStrategy;

    @Mock
    private CategoryAwareValidationStrategy teamworkStrategy;

    @Mock
    private CategoryAwareValidationStrategy passionStrategy;

    @Mock
    private CategoryAwareValidationStrategy masteryStrategy;

    private KudoValidationContext context;

    @BeforeEach
    void setUp() {
        when(innovationStrategy.supportedCategory()).thenReturn("Innovation");
        when(teamworkStrategy.supportedCategory()).thenReturn("Teamwork");
        when(passionStrategy.supportedCategory()).thenReturn("Passion");
        when(masteryStrategy.supportedCategory()).thenReturn("Mastery");

        context = new KudoValidationContext(
                List.of(innovationStrategy, teamworkStrategy, passionStrategy, masteryStrategy));
    }

    // ── TC-R12-001 — Resolves correct strategy by category ─────────────

    @Nested
    @DisplayName("TC-R12-001 — Category-based strategy resolution")
    class CategoryResolution {

        @Test
        @DisplayName("Teamwork request → only TeamworkValidationStrategy invoked")
        void validate_teamworkCategory_invokesCorrectStrategy() {
            KudoRequest request = buildRequest("Teamwork");

            context.validate(request);

            verify(teamworkStrategy).validate(request);
            verify(innovationStrategy, never()).validate(any());
            verify(passionStrategy, never()).validate(any());
            verify(masteryStrategy, never()).validate(any());
        }

        @Test
        @DisplayName("Innovation request → only InnovationValidationStrategy invoked")
        void validate_innovationCategory_invokesCorrectStrategy() {
            KudoRequest request = buildRequest("Innovation");

            context.validate(request);

            verify(innovationStrategy).validate(request);
            verify(teamworkStrategy, never()).validate(any());
        }

        @Test
        @DisplayName("Passion request → only PassionValidationStrategy invoked")
        void validate_passionCategory_invokesCorrectStrategy() {
            KudoRequest request = buildRequest("Passion");

            context.validate(request);

            verify(passionStrategy).validate(request);
            verify(teamworkStrategy, never()).validate(any());
        }

        @Test
        @DisplayName("Mastery request → only MasteryValidationStrategy invoked")
        void validate_masteryCategory_invokesCorrectStrategy() {
            KudoRequest request = buildRequest("Mastery");

            context.validate(request);

            verify(masteryStrategy).validate(request);
            verify(teamworkStrategy, never()).validate(any());
        }

        @Test
        @DisplayName("Category resolution is case-insensitive")
        void validate_caseInsensitive_resolvesCorrectly() {
            KudoRequest request = buildRequest("innovation");

            context.validate(request);

            verify(innovationStrategy).validate(request);
        }
    }

    // ── TC-R12-002 — Unsupported category ──────────────────────────────

    @Nested
    @DisplayName("TC-R12-002 — Unsupported category throws InvalidKudoException")
    class UnsupportedCategory {

        @Test
        @DisplayName("Unknown category 'Leadership' throws InvalidKudoException")
        void validate_unknownCategory_throwsException() {
            KudoRequest request = buildRequest("Leadership");

            assertThatThrownBy(() -> context.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Unsupported category: Leadership");

            verify(innovationStrategy, never()).validate(any());
            verify(teamworkStrategy, never()).validate(any());
            verify(passionStrategy, never()).validate(any());
            verify(masteryStrategy, never()).validate(any());
        }

        @Test
        @DisplayName("Null category throws InvalidKudoException")
        void validate_nullCategory_throwsException() {
            KudoRequest request = buildRequest(null);

            assertThatThrownBy(() -> context.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Category must not be null or empty");
        }

        @Test
        @DisplayName("Blank category throws InvalidKudoException")
        void validate_blankCategory_throwsException() {
            KudoRequest request = buildRequest("   ");

            assertThatThrownBy(() -> context.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Category must not be null or empty");
        }
    }

    // ── Helper ──────────────────────────────────────────────────────────

    private KudoRequest buildRequest(String category) {
        return KudoRequest.builder()
                .from("alice@sofka.com")
                .to("bob@sofka.com")
                .category(category)
                .message("A valid message that is long enough for any category.")
                .build();
    }
}
