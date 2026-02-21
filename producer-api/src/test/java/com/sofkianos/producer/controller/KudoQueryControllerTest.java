package com.sofkianos.producer.controller;

import com.sofkianos.producer.dto.KudoListItemDTO;
import com.sofkianos.producer.dto.PagedKudoResponse;
import com.sofkianos.producer.service.KudoQueryService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for the GET /api/v1/kudos endpoint.
 *
 * <p>Covers TC-001 (paginated structure), TC-002 (parameter validation),
 * and TC-007 (pagination navigation) at the HTTP layer.</p>
 *
 * <p>Uses {@code @WebMvcTest} with mocked {@link KudoQueryService}
 * to validate the controller contract without database.</p>
 */
@WebMvcTest(KudoQueryController.class)
@DisplayName("GET /api/v1/kudos — US-001: Listado público")
class KudoQueryControllerTest {

    private static final String ENDPOINT = "/api/v1/kudos";
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 2, 1, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KudoQueryService kudoQueryService;

    // ═══════════════════════════════════════════════════════════════════
    //  TC-001 — Endpoint GET retorna estructura paginada correcta
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-001: Estructura de respuesta paginada")
    class PaginatedResponseStructure {

        /**
         * Given la base de datos contiene 25 kudos activos
         * When ejecuto GET /api/v1/kudos sin parámetros
         * Then el status code es 200 OK
         * And la respuesta contiene fields: content, totalElements, totalPages, currentPage, size
         */
        @Test
        @DisplayName("GET sin parámetros retorna 200 OK con estructura paginada completa")
        void getKudos_withoutParams_returns200WithPagedStructure() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(20, 25, 2, 0, 20);
            when(kudoQueryService.listKudos(0, 20, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(20)))
                    .andExpect(jsonPath("$.totalElements").value(25))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        /**
         * Given la base de datos contiene kudos
         * When ejecuto GET /api/v1/kudos
         * Then cada item contiene: id, fromUser, toUser, category, message, createdAt
         * And createdAt está en formato ISO 8601
         */
        @Test
        @DisplayName("Cada item del content contiene todos los campos obligatorios")
        void getKudos_responseItemsContainAllRequiredFields() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(1, 1, 1, 0, 20);
            when(kudoQueryService.listKudos(0, 20, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").exists())
                    .andExpect(jsonPath("$.content[0].fromUser").exists())
                    .andExpect(jsonPath("$.content[0].toUser").exists())
                    .andExpect(jsonPath("$.content[0].category").exists())
                    .andExpect(jsonPath("$.content[0].message").exists())
                    .andExpect(jsonPath("$.content[0].createdAt").exists());
        }

        /**
         * Partición: Base de datos vacía → content=[], totalElements=0, totalPages=0
         */
        @Test
        @DisplayName("Base de datos vacía retorna 200 con content vacío")
        void getKudos_emptyDatabase_returns200WithEmptyContent() throws Exception {
            // Given
            PagedKudoResponse emptyResponse = new PagedKudoResponse(
                    Collections.emptyList(), 0, 0, 0, 20
            );
            when(kudoQueryService.listKudos(0, 20, "DESC")).thenReturn(emptyResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        /**
         * Verifica que el controller delega al service con los parámetros default correctos.
         */
        @Test
        @DisplayName("Sin parámetros, el controller delega con defaults: page=0, size=20, sortDirection=DESC")
        void getKudos_withoutParams_delegatesWithDefaults() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(0, 0, 0, 0, 20);
            when(kudoQueryService.listKudos(0, 20, "DESC")).thenReturn(mockResponse);

            // When
            mockMvc.perform(get(ENDPOINT))
                    .andExpect(status().isOk());

            // Then
            verify(kudoQueryService).listKudos(0, 20, "DESC");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TC-002 — Validación de límites en parámetros size y sortDirection
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-002: Validación de parámetros size y sortDirection")
    class ParameterValidation {

        // ── size: valores inválidos → 400 Bad Request ──────────────────

        /**
         * Given el endpoint tiene validación @Min(1) @Max(50) en size
         * When ejecuto GET /api/v1/kudos?size=0
         * Then el status code es 400 Bad Request
         */
        @Test
        @DisplayName("size=0 → 400 Bad Request (menor que mínimo)")
        void getKudos_sizeZero_returns400() throws Exception {
            mockMvc.perform(get(ENDPOINT).param("size", "0"))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Partición: size < 0 → inválido.
         */
        @ParameterizedTest(name = "size={0} → 400 Bad Request")
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("size negativo → 400 Bad Request")
        void getKudos_negativeSize_returns400(int size) throws Exception {
            mockMvc.perform(get(ENDPOINT).param("size", String.valueOf(size)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Valor Límite: size=51 → justo arriba del máximo → 400.
         */
        @Test
        @DisplayName("size=51 → 400 Bad Request (excede máximo)")
        void getKudos_size51_returns400() throws Exception {
            mockMvc.perform(get(ENDPOINT).param("size", "51"))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Partición: size > 50 → inválido (valores muy grandes).
         */
        @ParameterizedTest(name = "size={0} → 400 Bad Request")
        @ValueSource(ints = {100, 1000, 10000})
        @DisplayName("size muy grande → 400 Bad Request")
        void getKudos_veryLargeSize_returns400(int size) throws Exception {
            mockMvc.perform(get(ENDPOINT).param("size", String.valueOf(size)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifica que el body de error contiene estructura estándar sin stack trace.
         */
        @Test
        @DisplayName("Error de validación retorna body estructurado sin stack trace")
        void getKudos_invalidSize_returnsStructuredErrorBody() throws Exception {
            mockMvc.perform(get(ENDPOINT).param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.detail").exists())
                    // Must NOT contain Java stack trace
                    .andExpect(jsonPath("$.trace").doesNotExist());
        }

        // ── size: valores válidos → 200 OK ─────────────────────────────

        /**
         * Valor Límite: size=1 → límite inferior válido → 200 OK, retorna 1 item.
         */
        @Test
        @DisplayName("size=1 → 200 OK (límite inferior válido)")
        void getKudos_size1_returns200() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(1, 1, 1, 0, 1);
            when(kudoQueryService.listKudos(0, 1, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT).param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.size").value(1));
        }

        /**
         * Valor Límite: size=50 → límite superior válido → 200 OK.
         */
        @Test
        @DisplayName("size=50 → 200 OK (límite superior válido)")
        void getKudos_size50_returns200() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(50, 100, 2, 0, 50);
            when(kudoQueryService.listKudos(0, 50, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT).param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(50));
        }

        /**
         * Valor válido intermedio: size=25 → 200 OK.
         */
        @Test
        @DisplayName("size=25 → 200 OK (valor intermedio válido)")
        void getKudos_size25_returns200() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(25, 100, 4, 0, 25);
            when(kudoQueryService.listKudos(0, 25, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT).param("size", "25"))
                    .andExpect(status().isOk());
        }

        // ── sortDirection: valores inválidos → 400 Bad Request ─────────

        /**
         * Given el endpoint valida sortDirection con pattern ASC|DESC
         * When ejecuto GET /api/v1/kudos?sortDirection=INVALID
         * Then el status code es 400 Bad Request
         */
        @Test
        @DisplayName("sortDirection=INVALID → 400 Bad Request")
        void getKudos_invalidSortDirection_returns400() throws Exception {
            mockMvc.perform(get(ENDPOINT).param("sortDirection", "INVALID"))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Partición: sortDirection case-sensitive — "asc", "desc" no son válidos.
         */
        @ParameterizedTest(name = "sortDirection={0} → 400 Bad Request")
        @ValueSource(strings = {"asc", "desc", "Asc", "Desc", "RANDOM", "123"})
        @DisplayName("sortDirection en formato incorrecto → 400 Bad Request")
        void getKudos_wrongCaseSortDirection_returns400(String sortDirection) throws Exception {
            mockMvc.perform(get(ENDPOINT).param("sortDirection", sortDirection))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Partición: sortDirection vacío → 400 Bad Request.
         */
        @Test
        @DisplayName("sortDirection vacío → 400 Bad Request")
        void getKudos_emptySortDirection_returns400() throws Exception {
            mockMvc.perform(get(ENDPOINT).param("sortDirection", ""))
                    .andExpect(status().isBadRequest());
        }

        // ── sortDirection: valores válidos → 200 OK ────────────────────

        /**
         * Valor Límite: sortDirection=ASC → valor válido 1 → 200 OK.
         */
        @Test
        @DisplayName("sortDirection=ASC → 200 OK con orden ascendente")
        void getKudos_sortASC_returns200() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(20, 25, 2, 0, 20);
            when(kudoQueryService.listKudos(0, 20, "ASC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT).param("sortDirection", "ASC"))
                    .andExpect(status().isOk());

            verify(kudoQueryService).listKudos(0, 20, "ASC");
        }

        /**
         * Valor Límite: sortDirection=DESC → valor válido 2 → 200 OK.
         */
        @Test
        @DisplayName("sortDirection=DESC → 200 OK con orden descendente")
        void getKudos_sortDESC_returns200() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(20, 25, 2, 0, 20);
            when(kudoQueryService.listKudos(0, 20, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT).param("sortDirection", "DESC"))
                    .andExpect(status().isOk());

            verify(kudoQueryService).listKudos(0, 20, "DESC");
        }

        // ── Combinaciones de parámetros ────────────────────────────────

        /**
         * Given size=25 y sortDirection=ASC
         * When ejecuto GET /api/v1/kudos?size=25&sortDirection=ASC
         * Then el status code es 200 OK
         * And retorna 25 items ordenados ASC
         */
        @Test
        @DisplayName("size=25, sortDirection=ASC → 200 OK con parámetros combinados")
        void getKudos_validSizeAndSort_returns200() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(25, 100, 4, 0, 25);
            when(kudoQueryService.listKudos(0, 25, "ASC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT)
                            .param("size", "25")
                            .param("sortDirection", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(25)));

            verify(kudoQueryService).listKudos(0, 25, "ASC");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TC-007 — Paginación y ordenamiento respetan límites (HTTP layer)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-007: Navegación de paginación vía HTTP")
    class PaginationNavigation {

        /**
         * Given la base de datos contiene 47 kudos
         * When ejecuto GET /api/v1/kudos?page=0&size=20
         * Then retorna content con 20 items y totalPages=3
         */
        @Test
        @DisplayName("page=0, size=20 → 200 OK con 20 items y totalPages=3")
        void getKudos_firstPage_returns20ItemsWithCorrectMetadata() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(20, 47, 3, 0, 20);
            when(kudoQueryService.listKudos(0, 20, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(20)))
                    .andExpect(jsonPath("$.totalElements").value(47))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.currentPage").value(0));
        }

        /**
         * Given la base de datos contiene 47 kudos
         * When ejecuto GET /api/v1/kudos?page=2&size=20
         * Then retorna content con 7 items (última página parcial)
         */
        @Test
        @DisplayName("page=2 (última) → 200 OK con 7 items restantes")
        void getKudos_lastPage_returnsRemainingItems() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(7, 47, 3, 2, 20);
            when(kudoQueryService.listKudos(2, 20, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT)
                            .param("page", "2")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(7)))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.currentPage").value(2));
        }

        /**
         * Valor Límite: page=3 para 47 kudos (página inexistente) → content=[], totalElements=47.
         */
        @Test
        @DisplayName("Página inexistente → 200 OK con content vacío")
        void getKudos_nonExistentPage_returnsEmptyContent() throws Exception {
            // Given
            PagedKudoResponse mockResponse = new PagedKudoResponse(
                    Collections.emptyList(), 47, 3, 3, 20
            );
            when(kudoQueryService.listKudos(3, 20, "DESC")).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(get(ENDPOINT)
                            .param("page", "3")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(47));
        }

        /**
         * Verifica que el endpoint pasa correctamente los parámetros de paginación al service.
         */
        @Test
        @DisplayName("Parámetros de paginación se pasan correctamente al service")
        void getKudos_withAllParams_delegatesCorrectly() throws Exception {
            // Given
            PagedKudoResponse mockResponse = buildPagedResponse(10, 47, 5, 1, 10);
            when(kudoQueryService.listKudos(1, 10, "ASC")).thenReturn(mockResponse);

            // When
            mockMvc.perform(get(ENDPOINT)
                            .param("page", "1")
                            .param("size", "10")
                            .param("sortDirection", "ASC"))
                    .andExpect(status().isOk());

            // Then
            verify(kudoQueryService).listKudos(1, 10, "ASC");
        }

        /**
         * Valor Límite: page=0 ya se probó. page negativo debería resultar en 400.
         */
        @Test
        @DisplayName("page negativo → 400 Bad Request")
        void getKudos_negativePage_returns400() throws Exception {
            mockMvc.perform(get(ENDPOINT).param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    private PagedKudoResponse buildPagedResponse(int contentSize, long totalElements,
                                                  int totalPages, int currentPage, int size) {
        List<KudoListItemDTO> content = IntStream.rangeClosed(1, contentSize)
                .mapToObj(i -> new KudoListItemDTO(
                        (long) i,
                        "u***" + i + "@sofkianos.com",
                        "r***" + i + "@sofkianos.com",
                        "TEAMWORK",
                        "Great teamwork on project #" + i,
                        BASE_DATE.plusHours(i)
                ))
                .toList();

        return new PagedKudoResponse(content, totalElements, totalPages, currentPage, size);
    }
}
