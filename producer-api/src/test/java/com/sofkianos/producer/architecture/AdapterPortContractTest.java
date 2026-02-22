package com.sofkianos.producer.architecture;

import com.sofkianos.producer.domain.ports.out.KudoEventPublisher;
import com.sofkianos.producer.service.impl.KudoServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architectural contract tests for the Adapter/Port pattern (TC-R05-003).
 *
 * <p>Pure JUnit 5 tests — no Spring context loaded.
 * Validates that {@code KudoServiceImpl} depends <strong>only</strong> on the
 * {@link KudoEventPublisher} port interface, not on infrastructure classes like
 * {@code RabbitTemplate} or {@code ObjectMapper}.</p>
 *
 * <p><b>Principle</b>: Dependency Inversion (DIP) — the domain/service layer
 * MUST NOT know about infrastructure. If a developer accidentally injects
 * {@code RabbitTemplate} into the service, these tests catch the violation
 * at compile-test time.</p>
 *
 * <p><b>Risk covered</b>: Developer bypasses the Port/Adapter pattern by
 * injecting infrastructure dependencies directly into the service, coupling
 * business logic to RabbitMQ or Jackson.</p>
 */
@DisplayName("TC-R05-003: KudoServiceImpl NO importa RabbitTemplate ni ObjectMapper")
class AdapterPortContractTest {

    private static final List<String> FORBIDDEN_TYPES = List.of(
            "org.springframework.amqp.rabbit.core.RabbitTemplate",
            "com.fasterxml.jackson.databind.ObjectMapper"
    );

    private static final List<String> FORBIDDEN_PACKAGE_PREFIXES = List.of(
            "org.springframework.amqp",
            "com.fasterxml.jackson"
    );

    // ═══════════════════════════════════════════════════════════════════
    //  Fields — No infrastructure types in declared fields
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Fields — Sin tipos de infraestructura en campos")
    class FieldInspection {

        /**
         * Given la clase KudoServiceImpl del producer
         * When inspecciono sus campos declarados (getDeclaredFields)
         * Then ningún campo tiene tipo RabbitTemplate
         * And ningún campo tiene tipo ObjectMapper
         */
        @Test
        @DisplayName("KudoServiceImpl NO tiene campos RabbitTemplate ni ObjectMapper")
        void serviceImpl_hasNoInfrastructureFields() {
            // When
            Field[] fields = KudoServiceImpl.class.getDeclaredFields();
            List<String> fieldTypeNames = Arrays.stream(fields)
                    .map(f -> f.getType().getName())
                    .toList();

            // Then
            for (String forbiddenType : FORBIDDEN_TYPES) {
                assertThat(fieldTypeNames)
                        .as("KudoServiceImpl MUST NOT have a field of type %s — violates DIP",
                                forbiddenType)
                        .doesNotContain(forbiddenType);
            }
        }

        /**
         * Given la clase KudoServiceImpl del producer
         * When inspecciono sus campos declarados
         * Then al menos un campo es de tipo KudoEventPublisher (el port)
         */
        @Test
        @DisplayName("KudoServiceImpl tiene campo de tipo KudoEventPublisher (port)")
        void serviceImpl_hasDependencyOnPort() {
            // When
            List<Class<?>> fieldTypes = Arrays.stream(KudoServiceImpl.class.getDeclaredFields())
                    .map(Field::getType)
                    .toList();

            // Then
            assertThat(fieldTypes)
                    .as("KudoServiceImpl MUST depend on KudoEventPublisher port")
                    .contains(KudoEventPublisher.class);
        }

        /**
         * Given la clase KudoServiceImpl del producer
         * When inspecciono todos los tipos de sus campos
         * Then ningún campo tiene un tipo del paquete org.springframework.amqp
         * And ningún campo tiene un tipo del paquete com.fasterxml.jackson
         */
        @Test
        @DisplayName("KudoServiceImpl NO tiene campos de paquetes amqp ni jackson")
        void serviceImpl_hasNoFieldsFromForbiddenPackages() {
            // When
            List<String> fieldTypeNames = Arrays.stream(KudoServiceImpl.class.getDeclaredFields())
                    .map(f -> f.getType().getName())
                    .toList();

            // Then
            for (String fieldType : fieldTypeNames) {
                for (String forbiddenPrefix : FORBIDDEN_PACKAGE_PREFIXES) {
                    assertThat(fieldType)
                            .as("Field type '%s' belongs to forbidden package '%s' — DIP violation",
                                    fieldType, forbiddenPrefix)
                            .doesNotStartWith(forbiddenPrefix);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Constructor — No infrastructure types in constructor parameters
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Constructor — Sin tipos de infraestructura en parámetros")
    class ConstructorInspection {

        /**
         * Given la clase KudoServiceImpl del producer
         * When inspecciono los parámetros de todos sus constructores
         * Then ningún parámetro es RabbitTemplate
         * And ningún parámetro es ObjectMapper
         */
        @Test
        @DisplayName("Constructores de KudoServiceImpl NO reciben RabbitTemplate ni ObjectMapper")
        void serviceImpl_constructorsHaveNoInfrastructureParams() {
            // When
            Constructor<?>[] constructors = KudoServiceImpl.class.getDeclaredConstructors();

            for (Constructor<?> constructor : constructors) {
                List<String> paramTypes = Arrays.stream(constructor.getParameterTypes())
                        .map(Class::getName)
                        .toList();

                // Then
                for (String forbiddenType : FORBIDDEN_TYPES) {
                    assertThat(paramTypes)
                            .as("Constructor of KudoServiceImpl MUST NOT accept %s — DIP violation",
                                    forbiddenType)
                            .doesNotContain(forbiddenType);
                }
            }
        }

        /**
         * Given la clase KudoServiceImpl del producer
         * When inspecciono los parámetros de su constructor principal
         * Then todos los parámetros son interfaces (ports) o tipos de dominio
         */
        @Test
        @DisplayName("Constructor principal solo recibe interfaces/abstracciones (ports)")
        void serviceImpl_constructorOnlyReceivesPorts() {
            // When
            Constructor<?>[] constructors = KudoServiceImpl.class.getDeclaredConstructors();

            // Then — at least one constructor exists
            assertThat(constructors).isNotEmpty();

            // Find the constructor with the most parameters (Lombok @RequiredArgsConstructor)
            Constructor<?> mainConstructor = Arrays.stream(constructors)
                    .max((c1, c2) -> Integer.compare(
                            c1.getParameterCount(), c2.getParameterCount()))
                    .orElseThrow();

            for (Class<?> paramType : mainConstructor.getParameterTypes()) {
                for (String forbiddenPrefix : FORBIDDEN_PACKAGE_PREFIXES) {
                    assertThat(paramType.getName())
                            .as("Constructor param '%s' must not come from '%s'",
                                    paramType.getSimpleName(), forbiddenPrefix)
                            .doesNotStartWith(forbiddenPrefix);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Adapter compliance — RabbitMqKudoPublisher implements the port
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Adapter — RabbitMqKudoPublisher implementa KudoEventPublisher")
    class AdapterCompliance {

        /**
         * Given RabbitMqKudoPublisher es el adapter de infraestructura
         * When inspecciono sus interfaces implementadas
         * Then implementa KudoEventPublisher
         */
        @Test
        @DisplayName("RabbitMqKudoPublisher implementa la interfaz KudoEventPublisher")
        void adapter_implementsPort() {
            // When
            Class<?>[] interfaces = com.sofkianos.producer.infrastructure.messaging
                    .RabbitMqKudoPublisher.class.getInterfaces();

            // Then
            assertThat(interfaces)
                    .as("RabbitMqKudoPublisher MUST implement KudoEventPublisher port")
                    .contains(KudoEventPublisher.class);
        }

        /**
         * Given la separación Port/Adapter implementada
         * When verifico la ubicación de paquetes
         * Then el Port está en domain.ports.out
         * And el Adapter está en infrastructure.messaging
         */
        @Test
        @DisplayName("Port en domain.ports.out y Adapter en infrastructure.messaging")
        void portAndAdapter_areInCorrectPackages() {
            // Then
            assertThat(KudoEventPublisher.class.getPackageName())
                    .as("Port MUST be in domain package")
                    .contains("domain.ports.out");

            assertThat(com.sofkianos.producer.infrastructure.messaging
                    .RabbitMqKudoPublisher.class.getPackageName())
                    .as("Adapter MUST be in infrastructure package")
                    .contains("infrastructure.messaging");
        }
    }
}
