package com.sofkianos.producer.service;

import com.sofkianos.producer.dto.KudoListItemDTO;
import com.sofkianos.producer.dto.PagedKudoResponse;
import com.sofkianos.producer.entity.KudoEntity;
import com.sofkianos.producer.repository.KudoReadRepository;
import com.sofkianos.producer.service.impl.KudoQueryServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link KudoQueryServiceImpl}.
 *
 * <p>Covers TC-001 (paginated structure) and TC-007 (pagination + ordering limits).
 * Domain-first approach: validates that the service correctly orchestrates
 * repository calls and maps entities to DTOs with proper pagination metadata.</p>
 *
 * <p><b>Principle</b>: "Las pruebas dependen del contexto" — these tests
 * validate the read-model contract that the frontend depends on.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KudoQueryService — US-001: Listado público de Kudos")
class KudoQueryServiceTest {

    @Mock
    private KudoReadRepository kudoReadRepository;

    @InjectMocks
    private KudoQueryServiceImpl kudoQueryService;

    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 2, 1, 10, 0);

    // ═══════════════════════════════════════════════════════════════════
    //  TC-001 — Endpoint GET retorna estructura paginada correcta
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-001: Estructura paginada correcta")
    class PaginatedStructure {

        /**
         * Given la base de datos contiene 25 kudos activos
         * When ejecuto listKudos sin parámetros (defaults: page=0, size=20, DESC)
         * Then la respuesta contiene fields: content, totalElements, totalPages, currentPage, size
         * And content es un array con 20 items (default page size)
         */
        @Test
        @DisplayName("Sin parámetros retorna respuesta paginada con 20 items por defecto")
        void listKudos_withDefaultParams_returnsPagedResponseWith20Items() {
            // Given
            List<KudoEntity> allKudos = createKudoEntities(25);
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(allKudos.subList(0, 20), pageable, 25);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(20);
            assertThat(response.totalElements()).isEqualTo(25);
            assertThat(response.totalPages()).isEqualTo(2);
            assertThat(response.currentPage()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(20);
        }

        /**
         * Given la base de datos contiene 0 kudos
         * When ejecuto listKudos
         * Then content=[], totalElements=0, totalPages=0
         */
        @Test
        @DisplayName("Base de datos vacía retorna content vacío con totales en cero")
        void listKudos_withEmptyDatabase_returnsEmptyContent() {
            // Given
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
            assertThat(response.totalPages()).isZero();
        }

        /**
         * Given la base de datos contiene kudos
         * When ejecuto listKudos
         * Then cada item contiene: id, fromUser, toUser, category, message, createdAt
         */
        @Test
        @DisplayName("Cada item contiene todos los campos obligatorios del contrato")
        void listKudos_eachItemContainsAllRequiredFields() {
            // Given
            List<KudoEntity> kudos = createKudoEntities(1);
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(kudos, pageable, 1);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response.content()).hasSize(1);
            KudoListItemDTO item = response.content().get(0);
            assertThat(item.id()).isNotNull();
            assertThat(item.fromUser()).isNotNull().isNotBlank();
            assertThat(item.toUser()).isNotNull().isNotBlank();
            assertThat(item.category()).isNotNull().isNotBlank();
            assertThat(item.message()).isNotNull().isNotBlank();
            assertThat(item.createdAt()).isNotNull();
        }

        /**
         * Partición de Equivalencia: Base de datos con exactamente 1 kudo.
         * Valor Límite: content=[1 item], totalElements=1, totalPages=1.
         */
        @Test
        @DisplayName("Base de datos con 1 kudo retorna 1 elemento y 1 página")
        void listKudos_withSingleKudo_returnsSingleItemAndOnePage() {
            // Given
            List<KudoEntity> kudos = createKudoEntities(1);
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(kudos, pageable, 1);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response.content()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
            assertThat(response.totalPages()).isEqualTo(1);
        }

        /**
         * Verifica que el servicio construye el Pageable correcto con sort DESC por createdAt.
         */
        @Test
        @DisplayName("Servicio delega al repository con Pageable correcto (DESC por createdAt)")
        void listKudos_constructsCorrectPageableWithDescSort() {
            // Given
            Pageable expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> emptyPage = new PageImpl<>(Collections.emptyList(), expectedPageable, 0);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(kudoReadRepository).findAll(pageableCaptor.capture());

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isZero();
            assertThat(captured.getPageSize()).isEqualTo(20);
            assertThat(captured.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(captured.getSort().getOrderFor("createdAt").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }

        /**
         * Valor Límite: Exactamente 20 kudos = 1 página completa.
         */
        @Test
        @DisplayName("20 kudos exactos retorna 1 página completa")
        void listKudos_exactlyOnePage_returnsTotalPagesOne() {
            // Given
            List<KudoEntity> kudos = createKudoEntities(20);
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(kudos, pageable, 20);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response.content()).hasSize(20);
            assertThat(response.totalPages()).isEqualTo(1);
        }

        /**
         * Valor Límite: 21 kudos → más de 1 página → totalPages=2.
         */
        @Test
        @DisplayName("21 kudos retorna totalPages=2 (overflow de 1 página)")
        void listKudos_21kudos_returnsTotalPagesTwo() {
            // Given
            List<KudoEntity> kudos = createKudoEntities(20); // first page
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(kudos, pageable, 21);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response.content()).hasSize(20);
            assertThat(response.totalElements()).isEqualTo(21);
            assertThat(response.totalPages()).isEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TC-007 — Paginación y ordenamiento respetan límites
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-007: Paginación y ordenamiento")
    class PaginationAndOrdering {

        /**
         * Given la base de datos contiene 47 kudos
         * When ejecuto listKudos con page=0, size=20
         * Then retorna content con 20 items
         * And totalElements es 47
         * And totalPages es 3
         * And currentPage es 0
         */
        @Test
        @DisplayName("47 kudos, page=0, size=20 → 20 items, totalPages=3, currentPage=0")
        void listKudos_firstPageOf47_returns20ItemsAndTotalPages3() {
            // Given
            List<KudoEntity> page0Kudos = createKudoEntities(20);
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(page0Kudos, pageable, 47);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            assertThat(response.content()).hasSize(20);
            assertThat(response.totalElements()).isEqualTo(47);
            assertThat(response.totalPages()).isEqualTo(3);
            assertThat(response.currentPage()).isEqualTo(0);
        }

        /**
         * Given la base de datos contiene 47 kudos
         * When ejecuto listKudos con page=2, size=20
         * Then retorna content con 7 items (última página)
         * And totalPages es 3
         * And currentPage es 2
         */
        @Test
        @DisplayName("47 kudos, page=2, size=20 → 7 items (última página)")
        void listKudos_lastPageOf47_returns7Items() {
            // Given
            List<KudoEntity> lastPageKudos = createKudoEntities(7);
            Pageable pageable = PageRequest.of(2, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(lastPageKudos, pageable, 47);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(2, 20, "DESC");

            // Then
            assertThat(response.content()).hasSize(7);
            assertThat(response.totalPages()).isEqualTo(3);
            assertThat(response.currentPage()).isEqualTo(2);
        }

        /**
         * Valor Límite: page=99 (fuera de rango) → retorna content vacío.
         * totalElements sigue siendo 47.
         */
        @Test
        @DisplayName("Página fuera de rango retorna content vacío con totalElements correcto")
        void listKudos_outOfRangePage_returnsEmptyContent() {
            // Given
            Pageable pageable = PageRequest.of(99, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 47);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(99, 20, "DESC");

            // Then
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(47);
        }

        /**
         * Given kudos con fechas incrementales
         * When ejecuto listKudos con sortDirection=DESC
         * Then el primer item tiene la fecha más reciente
         * And el último item tiene fecha anterior al primero
         */
        @Test
        @DisplayName("Ordenamiento DESC: primer item tiene fecha más reciente")
        void listKudos_sortDESC_firstItemHasMostRecentDate() {
            // Given — entities ordered DESC by createdAt
            List<KudoEntity> kudos = createKudoEntitiesWithIncrementalDates(5);
            // Reverse to simulate DESC order from DB
            List<KudoEntity> descOrdered = new ArrayList<>(kudos);
            Collections.reverse(descOrdered);

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(descOrdered, pageable, 5);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "DESC");

            // Then
            List<KudoListItemDTO> items = response.content();
            assertThat(items).hasSizeGreaterThan(1);
            assertThat(items.get(0).createdAt()).isAfter(items.get(items.size() - 1).createdAt());
        }

        /**
         * Given kudos con fechas incrementales
         * When ejecuto listKudos con sortDirection=ASC
         * Then el primer item tiene la fecha más antigua
         * And los items están en orden cronológico ascendente
         */
        @Test
        @DisplayName("Ordenamiento ASC: primer item tiene fecha más antigua")
        void listKudos_sortASC_firstItemHasOldestDate() {
            // Given — entities ordered ASC by createdAt
            List<KudoEntity> ascOrdered = createKudoEntitiesWithIncrementalDates(5);

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(ascOrdered, pageable, 5);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 20, "ASC");

            // Then
            List<KudoListItemDTO> items = response.content();
            assertThat(items).hasSizeGreaterThan(1);
            assertThat(items.get(0).createdAt()).isBefore(items.get(items.size() - 1).createdAt());
        }

        /**
         * Verifica que el servicio construye Pageable con ASC cuando se solicita.
         */
        @Test
        @DisplayName("sortDirection=ASC construye Pageable con Sort.Direction.ASC")
        void listKudos_sortASC_constructsPageableWithAscDirection() {
            // Given
            Pageable expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<KudoEntity> emptyPage = new PageImpl<>(Collections.emptyList(), expectedPageable, 0);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            kudoQueryService.listKudos(0, 20, "ASC");

            // Then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(kudoReadRepository).findAll(pageableCaptor.capture());

            assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }

        /**
         * Valor Límite: size=1 → paginación mínima → totalPages=47 para 47 kudos.
         */
        @Test
        @DisplayName("size=1, 47 kudos → 1 item por página, totalPages=47")
        void listKudos_minPageSize_returns1ItemPerPage() {
            // Given
            List<KudoEntity> singleItem = createKudoEntities(1);
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(singleItem, pageable, 47);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 1, "DESC");

            // Then
            assertThat(response.content()).hasSize(1);
            assertThat(response.totalPages()).isEqualTo(47);
        }

        /**
         * Valor Límite: size=50 → paginación máxima → 47 items en 1 página.
         */
        @Test
        @DisplayName("size=50, 47 kudos → todos los items en 1 página")
        void listKudos_maxPageSize_returnsAllItemsInOnePage() {
            // Given
            List<KudoEntity> allKudos = createKudoEntities(47);
            Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<KudoEntity> page = new PageImpl<>(allKudos, pageable, 47);
            when(kudoReadRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PagedKudoResponse response = kudoQueryService.listKudos(0, 50, "DESC");

            // Then
            assertThat(response.content()).hasSize(47);
            assertThat(response.totalPages()).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers — Factories de entidades de prueba
    // ═══════════════════════════════════════════════════════════════════

    private List<KudoEntity> createKudoEntities(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> buildKudoEntity(
                        (long) i,
                        "user" + i + "@sofkianos.com",
                        "recipient" + i + "@sofkianos.com",
                        "TEAMWORK",
                        "Great job on project #" + i,
                        BASE_DATE.plusHours(i)
                ))
                .toList();
    }

    private List<KudoEntity> createKudoEntitiesWithIncrementalDates(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> buildKudoEntity(
                        (long) i,
                        "sender" + i + "@sofkianos.com",
                        "receiver" + i + "@sofkianos.com",
                        i % 2 == 0 ? "INNOVATION" : "TEAMWORK",
                        "Kudo message #" + i,
                        BASE_DATE.plusDays(i)
                ))
                .toList();
    }

    private KudoEntity buildKudoEntity(Long id, String fromUser, String toUser,
                                        String category, String message,
                                        LocalDateTime createdAt) {
        return new KudoEntity(id, fromUser, toUser, category, message, createdAt);
    }
}
