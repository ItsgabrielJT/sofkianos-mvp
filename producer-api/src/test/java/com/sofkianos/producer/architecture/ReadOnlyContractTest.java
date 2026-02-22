package com.sofkianos.producer.architecture;

import com.sofkianos.producer.entity.KudoEntity;
import com.sofkianos.producer.service.KudoQueryService;
import com.sofkianos.producer.service.impl.KudoQueryServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architectural contract tests for the read-only guarantee (TC-003).
 *
 * <p>Pure JUnit 5 tests — no Spring context loaded.
 * Validates structural invariants that enforce the read-only contract
 * in the Producer API's query side.</p>
 *
 * <p><b>Principle</b>: "Las pruebas dependen del contexto" — in a CQRS
 * architecture, the query side MUST be structurally incapable of modifying state.
 * These tests act as a safety net against accidental introduction of write paths.</p>
 *
 * <p><b>Risk covered</b>: Developer accidentally adds write operations to the
 * Producer API query side, bypassing the Consumer Worker's exclusive write role.</p>
 */
@DisplayName("TC-003: Contrato arquitectónico read-only — Pure JUnit 5")
class ReadOnlyContractTest {

    // ═══════════════════════════════════════════════════════════════════
    //  Service Layer — @Transactional(readOnly = true)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Service Layer — Anotación @Transactional(readOnly = true)")
    class ServiceTransactionalContract {

        /**
         * Given KudoQueryServiceImpl es la implementación del query side
         * When inspecciono las anotaciones de clase
         * Then tiene @Transactional(readOnly = true) a nivel de clase
         *
         * <p>This ensures Hibernate sets FlushMode.MANUAL and the JDBC
         * connection hint readOnly=true for ALL methods in the service.</p>
         */
        @Test
        @DisplayName("KudoQueryServiceImpl tiene @Transactional(readOnly=true) a nivel de clase")
        void serviceImpl_hasTransactionalReadOnly_atClassLevel() {
            // When
            Transactional annotation = KudoQueryServiceImpl.class.getAnnotation(Transactional.class);

            // Then
            assertThat(annotation)
                    .as("KudoQueryServiceImpl MUST have @Transactional at class level")
                    .isNotNull();

            assertThat(annotation.readOnly())
                    .as("@Transactional MUST be readOnly=true to enforce CQRS read-only contract")
                    .isTrue();
        }

        /**
         * Given KudoQueryServiceImpl hereda @Transactional(readOnly = true)
         * When inspecciono los métodos públicos
         * Then NINGÚN método tiene @Transactional con readOnly=false que override la clase
         *
         * <p>Prevents a developer from accidentally annotating a method with
         * @Transactional (without readOnly) which would override the class-level setting.</p>
         */
        @Test
        @DisplayName("Ningún método del service sobreescribe @Transactional con readOnly=false")
        void serviceImpl_noMethodOverridesReadOnlyToFalse() {
            // When
            Method[] methods = KudoQueryServiceImpl.class.getDeclaredMethods();

            // Then
            for (Method method : methods) {
                Transactional methodAnnotation = method.getAnnotation(Transactional.class);
                if (methodAnnotation != null) {
                    assertThat(methodAnnotation.readOnly())
                            .as("Method %s MUST NOT override class-level readOnly=true with readOnly=false",
                                    method.getName())
                            .isTrue();
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Entity Layer — Immutability (no setters)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Entity Layer — Inmutabilidad de KudoEntity")
    class EntityImmutability {

        /**
         * Given KudoEntity es una entidad de solo lectura
         * When inspecciono los métodos públicos
         * Then NO tiene métodos setter (set*)
         *
         * <p>Setters are intentionally omitted from KudoEntity to enforce
         * the read-only contract. Only @Getter is applied via Lombok.
         * Write operations are handled by Consumer Worker's own entity model.</p>
         */
        @Test
        @DisplayName("KudoEntity NO tiene métodos setter — inmutabilidad garantizada")
        void kudoEntity_hasNoSetterMethods() {
            // When
            List<String> setterMethods = Arrays.stream(KudoEntity.class.getDeclaredMethods())
                    .filter(m -> Modifier.isPublic(m.getModifiers()))
                    .map(Method::getName)
                    .filter(name -> name.startsWith("set"))
                    .toList();

            // Then
            assertThat(setterMethods)
                    .as("KudoEntity MUST NOT have setter methods — read-only entity contract")
                    .isEmpty();
        }

        /**
         * Given KudoEntity tiene campos de dominio
         * When inspecciono los métodos públicos
         * Then tiene getters para todos los campos requeridos
         */
        @Test
        @DisplayName("KudoEntity tiene getters para todos los campos obligatorios")
        void kudoEntity_hasGettersForAllRequiredFields() {
            // Given
            List<String> requiredGetters = List.of(
                    "getId", "getFromUser", "getToUser",
                    "getCategory", "getMessage", "getCreatedAt"
            );

            // When
            List<String> actualMethods = Arrays.stream(KudoEntity.class.getDeclaredMethods())
                    .filter(m -> Modifier.isPublic(m.getModifiers()))
                    .map(Method::getName)
                    .toList();

            // Then
            assertThat(actualMethods)
                    .as("KudoEntity MUST have getters for all domain fields")
                    .containsAll(requiredGetters);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Interface Layer — Read-only method signatures
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Interface Layer — KudoQueryService solo expone métodos de lectura")
    class InterfaceReadOnlyContract {

        private static final List<String> WRITE_METHOD_PREFIXES = List.of(
                "save", "delete", "update", "remove", "insert",
                "create", "modify", "persist", "merge"
        );

        /**
         * Given KudoQueryService es la interfaz del query side (CQRS)
         * When inspecciono los métodos declarados
         * Then NO contiene métodos con nombres de escritura (save, delete, update...)
         *
         * <p>CQRS principle: the query interface MUST only expose read operations.
         * Write operations belong to the command side (KudoService + Consumer Worker).</p>
         */
        @Test
        @DisplayName("KudoQueryService NO expone métodos de escritura")
        void queryServiceInterface_hasNoWriteMethods() {
            // When
            List<String> methodNames = Arrays.stream(KudoQueryService.class.getDeclaredMethods())
                    .map(Method::getName)
                    .toList();

            // Then
            for (String methodName : methodNames) {
                assertThat(WRITE_METHOD_PREFIXES.stream().anyMatch(methodName::startsWith))
                        .as("Method '%s' in KudoQueryService looks like a write operation — violates CQRS",
                                methodName)
                        .isFalse();
            }
        }

        /**
         * Given KudoQueryService es la interfaz del query side
         * When inspecciono los métodos declarados
         * Then solo contiene métodos de lectura (list*, find*, get*, count*)
         */
        @Test
        @DisplayName("KudoQueryService solo tiene métodos con nombres de lectura")
        void queryServiceInterface_onlyHasReadMethods() {
            // Given
            List<String> readMethodPrefixes = List.of("list", "find", "get", "count", "search", "query");

            // When
            List<String> methodNames = Arrays.stream(KudoQueryService.class.getDeclaredMethods())
                    .map(Method::getName)
                    .toList();

            // Then
            assertThat(methodNames).isNotEmpty();
            for (String methodName : methodNames) {
                assertThat(readMethodPrefixes.stream().anyMatch(methodName::startsWith))
                        .as("Method '%s' in KudoQueryService MUST have a read-oriented name",
                                methodName)
                        .isTrue();
            }
        }
    }
}
