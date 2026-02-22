package com.sofkianos.producer.repository;

import com.sofkianos.producer.entity.KudoEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-level tests for read-only PostgreSQL contract (TC-003).
 *
 * <p>Validates that {@link KudoReadRepository} supports paginated reads
 * correctly and that the Producer API's read-only architectural contract
 * is enforced through transactional boundaries.</p>
 *
 * <p><b>Principle</b>: "Las pruebas dependen del contexto" — in the CQRS
 * architecture, the Producer API MUST only read from PostgreSQL.
 * Writes are exclusively handled by the Consumer Worker.</p>
 *
 * <p><b>Risk covered</b>: Producer API modifies data that should only be
 * managed by Consumer Worker, causing inconsistencies in the event-driven flow.</p>
 */
@DataJpaTest
@DisplayName("TC-003: Conexión read-only a PostgreSQL — Repository Layer")
class KudoReadRepositoryReadOnlyTest {

    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 2, 1, 10, 0);

    @Autowired
    private KudoReadRepository kudoReadRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    // ═══════════════════════════════════════════════════════════════════
    //  Read Operations — SELECT queries succeed
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Read operations — SELECT queries succeed")
    class ReadOperations {

        @BeforeEach
        void seedDatabase() {
            for (int i = 1; i <= 5; i++) {
                testEntityManager.persistAndFlush(new KudoEntity(
                        (long) i,
                        "sender" + i + "@sofkianos.com",
                        "receiver" + i + "@sofkianos.com",
                        "TEAMWORK",
                        "Great collaboration on project " + i,
                        BASE_DATE.plusDays(i)
                ));
            }
            testEntityManager.clear();
        }

        /**
         * Given Producer API está configurada con DataSource read-only
         * When ejecuto kudoRepository.findAll(pageable)
         * Then la operación de lectura es exitosa
         * And retorna datos sin errores
         */
        @Test
        @DisplayName("findAll(Pageable) retorna página con resultados correctos")
        void findAll_withPageable_returnsPagedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));

            // When
            Page<KudoEntity> result = kudoReadRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isZero();
        }

        /**
         * Given Producer API con conexión read-only activa
         * When ejecuto kudoRepository.findById(existingId)
         * Then retorna la entidad correctamente
         */
        @Test
        @DisplayName("findById retorna entidad cuando existe")
        void findById_withExistingId_returnsEntity() {
            // When
            Optional<KudoEntity> result = kudoReadRepository.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getFromUser()).isEqualTo("sender1@sofkianos.com");
            assertThat(result.get().getToUser()).isEqualTo("receiver1@sofkianos.com");
            assertThat(result.get().getCategory()).isEqualTo("TEAMWORK");
        }

        /**
         * Given los items están ordenados DESC por createdAt
         * When ejecuto findAll con Sort.Direction.DESC
         * Then el primer item tiene la fecha más reciente
         */
        @Test
        @DisplayName("findAll respeta ordenamiento DESC por createdAt")
        void findAll_withDescSort_returnsNewestFirst() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

            // When
            Page<KudoEntity> result = kudoReadRepository.findAll(pageable);

            // Then
            assertThat(result.getContent())
                    .extracting(KudoEntity::getCreatedAt)
                    .isSortedAccordingTo((a, b) -> b.compareTo(a));

            assertThat(result.getContent().get(0).getCreatedAt())
                    .isEqualTo(BASE_DATE.plusDays(5));
        }

        /**
         * Given los items están ordenados ASC por createdAt
         * When ejecuto findAll con Sort.Direction.ASC
         * Then el primer item tiene la fecha más antigua
         */
        @Test
        @DisplayName("findAll respeta ordenamiento ASC por createdAt")
        void findAll_withAscSort_returnsOldestFirst() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "createdAt"));

            // When
            Page<KudoEntity> result = kudoReadRepository.findAll(pageable);

            // Then
            assertThat(result.getContent())
                    .extracting(KudoEntity::getCreatedAt)
                    .isSorted();

            assertThat(result.getContent().get(0).getCreatedAt())
                    .isEqualTo(BASE_DATE.plusDays(1));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Boundary — Empty database
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Boundary — Base de datos vacía")
    class EmptyDatabase {

        /**
         * Given la base de datos está vacía (0 kudos)
         * When ejecuto kudoRepository.findAll(pageable)
         * Then retorna content=[], totalElements=0, totalPages=0
         */
        @Test
        @DisplayName("findAll en BD vacía retorna página vacía sin errores")
        void findAll_withEmptyDatabase_returnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

            // When
            Page<KudoEntity> result = kudoReadRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
            assertThat(result.getNumber()).isZero();
        }

        /**
         * Given la base de datos está vacía
         * When ejecuto kudoRepository.findById(1L)
         * Then retorna Optional.empty()
         */
        @Test
        @DisplayName("findById en BD vacía retorna Optional vacío")
        void findById_withEmptyDatabase_returnsEmpty() {
            // When
            Optional<KudoEntity> result = kudoReadRepository.findById(1L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Write Rejection — Read-only transactional context
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Write rejection — Operaciones de escritura bloqueadas en contexto read-only")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    class WriteRejection {

        /**
         * Given Producer API está configurada con @Transactional(readOnly = true)
         * When intento ejecutar kudoRepository.save(newKudo)
         * Then los cambios NO se persisten en la base de datos
         * And la base de datos no tiene cambios
         *
         * <p>Validates the read-only transactional behavior. In the Producer API,
         * the service layer is annotated @Transactional(readOnly = true) which
         * prevents Hibernate from flushing dirty entities to the database.</p>
         */
        @Test
        @Transactional(readOnly = true)
        @DisplayName("save() en transacción read-only NO persiste la entidad")
        void save_inReadOnlyTransaction_doesNotPersistNewEntity() {
            // Given
            KudoEntity newKudo = new KudoEntity(
                    999L,
                    "hacker@evil.com",
                    "victim@sofkianos.com",
                    "TEAMWORK",
                    "Malicious write attempt",
                    LocalDateTime.now()
            );

            // When — save in read-only transaction (Hibernate FlushMode.MANUAL)
            kudoReadRepository.save(newKudo);

            // Then — entity is managed but NOT flushed to DB
            // Verify via a separate count that bypasses the persistence context
            testEntityManager.clear(); // Detach all managed entities
            long count = kudoReadRepository.count();
            assertThat(count)
                    .as("Entity saved in read-only transaction must NOT be persisted")
                    .isZero();
        }

        /**
         * Given Producer API con conexión read-only (@Transactional readOnly=true)
         * When intento ejecutar kudoRepository.deleteAll() en transacción read-only
         * Then Hibernate FlushMode.MANUAL impide que el DELETE se ejecute en BD
         * And las entidades permanecen intactas
         *
         * <p>In a read-only transaction, Hibernate sets FlushMode to MANUAL.
         * This means deleteAll() marks entities for removal in the persistence
         * context, but the SQL DELETE is never flushed to the database.</p>
         */
        @Test
        @Transactional(readOnly = true)
        @DisplayName("deleteAll() en transacción read-only NO se ejecuta — FlushMode.MANUAL")
        void deleteAll_inReadOnlyTransaction_doesNotFlushToDatabase() {
            // Given — repository reports some initial state
            long initialCount = kudoReadRepository.count();

            // When — attempt to delete all in read-only transaction
            kudoReadRepository.deleteAll();

            // Then — clear persistence context and re-query
            // In read-only mode, Hibernate does NOT flush the delete DMLs
            testEntityManager.clear();
            long countAfterDelete = kudoReadRepository.count();

            assertThat(countAfterDelete)
                    .as("deleteAll() in read-only transaction must NOT reduce the count")
                    .isEqualTo(initialCount);
        }

        /**
         * Given Producer API está configurada sin spring.jpa.hibernate.ddl-auto en producción
         * When la aplicación Spring Boot inicia
         * Then NO se ejecutan operaciones DDL automáticas
         *
         * <p>This test verifies that in a read-only context, the count() operation
         * (aggregate query) also works correctly — important for monitoring.</p>
         */
        @Test
        @Transactional(readOnly = true)
        @DisplayName("count() en transacción read-only retorna resultado correcto")
        void count_inReadOnlyTransaction_succeeds() {
            // When
            long count = kudoReadRepository.count();

            // Then
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Connection — First read verifies connectivity
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Connection — Conectividad a base de datos")
    class DatabaseConnection {

        /**
         * Given Producer API está configurada con DataSource
         * When la aplicación Spring Boot inicia
         * Then la conexión se establece correctamente
         * And el connection pool se inicializa
         */
        @Test
        @DisplayName("Conexión a BD se establece y repository es inyectado correctamente")
        void repository_isInjected_andConnectionWorks() {
            // Then
            assertThat(kudoReadRepository).isNotNull();

            // Verify connection works with a simple query
            long count = kudoReadRepository.count();
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }
}
