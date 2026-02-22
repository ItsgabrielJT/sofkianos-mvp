package com.sofkianos.producer.domain.validation;

import com.sofkianos.producer.dto.KudoRequest;
import com.sofkianos.producer.exception.InvalidKudoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for all concrete {@code CategoryAwareValidationStrategy} implementations.
 *
 * <p>Covers TC-R12-005 — Each strategy is independent and validates its own rules.</p>
 */
class ValidationStrategyTest {

    // ── InnovationValidationStrategy ────────────────────────────────────

    @Nested
    @DisplayName("InnovationValidationStrategy")
    class InnovationTests {

        private final InnovationValidationStrategy strategy = new InnovationValidationStrategy();

        @Test
        @DisplayName("Reports 'Innovation' as supported category")
        void supportedCategory() {
            org.assertj.core.api.Assertions.assertThat(strategy.supportedCategory())
                    .isEqualTo("Innovation");
        }

        @Test
        @DisplayName("Valid request passes validation")
        void validate_validRequest_passes() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "A great innovative idea that is well described");
            assertThatCode(() -> strategy.validate(request)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Self-kudo throws InvalidKudoException")
        void validate_selfKudo_throws() {
            KudoRequest request = buildRequest("alice@sofka.com", "alice@sofka.com",
                    "A great innovative idea that is well described");
            assertThatThrownBy(() -> strategy.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Cannot send a kudo to yourself");
        }

        @Test
        @DisplayName("Short message (< 20 chars) throws InvalidKudoException")
        void validate_shortMessage_throws() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Short msg");
            assertThatThrownBy(() -> strategy.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Innovation kudos require a message of at least 20 characters");
        }
    }

    // ── TeamworkValidationStrategy ──────────────────────────────────────

    @Nested
    @DisplayName("TeamworkValidationStrategy")
    class TeamworkTests {

        private final TeamworkValidationStrategy strategy = new TeamworkValidationStrategy();

        @Test
        @DisplayName("Reports 'Teamwork' as supported category")
        void supportedCategory() {
            org.assertj.core.api.Assertions.assertThat(strategy.supportedCategory())
                    .isEqualTo("Teamwork");
        }

        @Test
        @DisplayName("Valid request passes validation")
        void validate_validRequest_passes() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Great collaboration on the project");
            assertThatCode(() -> strategy.validate(request)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Self-kudo throws InvalidKudoException")
        void validate_selfKudo_throws() {
            KudoRequest request = buildRequest("bob@sofka.com", "BOB@sofka.com",
                    "Great collaboration on the project");
            assertThatThrownBy(() -> strategy.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Cannot send a kudo to yourself");
        }

        @Test
        @DisplayName("Short message (< 15 chars) throws InvalidKudoException")
        void validate_shortMessage_throws() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Too short");
            assertThatThrownBy(() -> strategy.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Teamwork kudos require a message of at least 15 characters");
        }
    }

    // ── PassionValidationStrategy ───────────────────────────────────────

    @Nested
    @DisplayName("PassionValidationStrategy")
    class PassionTests {

        private final PassionValidationStrategy strategy = new PassionValidationStrategy();

        @Test
        @DisplayName("Reports 'Passion' as supported category")
        void supportedCategory() {
            org.assertj.core.api.Assertions.assertThat(strategy.supportedCategory())
                    .isEqualTo("Passion");
        }

        @Test
        @DisplayName("Valid request passes validation")
        void validate_validRequest_passes() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Keep up the great energy!!!");
            assertThatCode(() -> strategy.validate(request)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Short message (< 10 chars) throws InvalidKudoException")
        void validate_shortMessage_throws() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Nice");
            assertThatThrownBy(() -> strategy.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Passion kudos require a message of at least 10 characters");
        }
    }

    // ── MasteryValidationStrategy ───────────────────────────────────────

    @Nested
    @DisplayName("MasteryValidationStrategy")
    class MasteryTests {

        private final MasteryValidationStrategy strategy = new MasteryValidationStrategy();

        @Test
        @DisplayName("Reports 'Mastery' as supported category")
        void supportedCategory() {
            org.assertj.core.api.Assertions.assertThat(strategy.supportedCategory())
                    .isEqualTo("Mastery");
        }

        @Test
        @DisplayName("Valid request passes validation")
        void validate_validRequest_passes() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Exceptional expertise on the cloud architecture redesign");
            assertThatCode(() -> strategy.validate(request)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Short message (< 25 chars) throws InvalidKudoException")
        void validate_shortMessage_throws() {
            KudoRequest request = buildRequest("alice@sofka.com", "bob@sofka.com",
                    "Great skills shown");
            assertThatThrownBy(() -> strategy.validate(request))
                    .isInstanceOf(InvalidKudoException.class)
                    .hasMessageContaining("Mastery kudos require a message of at least 25 characters");
        }
    }

    // ── Cross-strategy: OCP compliance ──────────────────────────────────

    @Nested
    @DisplayName("TC-R12-005 — OCP compliance")
    class ArchitectureTests {

        @ParameterizedTest
        @ValueSource(classes = {
                InnovationValidationStrategy.class,
                TeamworkValidationStrategy.class,
                PassionValidationStrategy.class,
                MasteryValidationStrategy.class
        })
        @DisplayName("Each strategy is in its own class (not inner)")
        void eachStrategy_isTopLevelOrStaticClass(Class<?> strategyClass) {
            org.assertj.core.api.Assertions.assertThat(strategyClass.getEnclosingClass())
                    .as("Strategy %s must be a top-level class, not an inner class", strategyClass.getSimpleName())
                    .isNull();
        }

        @ParameterizedTest
        @ValueSource(classes = {
                InnovationValidationStrategy.class,
                TeamworkValidationStrategy.class,
                PassionValidationStrategy.class,
                MasteryValidationStrategy.class
        })
        @DisplayName("Each strategy implements CategoryAwareValidationStrategy")
        void eachStrategy_implementsInterface(Class<?> strategyClass) {
            org.assertj.core.api.Assertions.assertThat(
                    com.sofkianos.producer.domain.ports.in.validation.CategoryAwareValidationStrategy.class
                            .isAssignableFrom(strategyClass))
                    .as("%s must implement CategoryAwareValidationStrategy", strategyClass.getSimpleName())
                    .isTrue();
        }
    }

    // ── Helper ──────────────────────────────────────────────────────────

    private static KudoRequest buildRequest(String from, String to, String message) {
        return KudoRequest.builder()
                .from(from)
                .to(to)
                .category("Innovation")
                .message(message)
                .build();
    }
}
