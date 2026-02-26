package com.sofkianos.producer.controller;

import com.sofkianos.producer.application.dto.KudoListItemDTO;
import com.sofkianos.producer.application.ports.in.KudoService;
import com.sofkianos.producer.domain.model.PagedResult;
import com.sofkianos.producer.infrastructure.inbound.web.KudosController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KudosController.class)
class KudosControllerGetTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private KudoService kudoService;

        @Test
        @DisplayName("GET /api/v1/kudos should return 200 OK with content")
        void getKudos_ReturnsOkWithContent() throws Exception {
                var kudo = KudoListItemDTO.builder()
                                .receptor("Maria G.")
                                .emisor("Juan P.")
                                .mensaje("Gracias!")
                                .fecha(LocalDateTime.now())
                                .categoria("Teamwork")
                                .build();

                var pagedResult = new PagedResult<>(List.of(kudo), 1, 1, 0, 10);

                when(kudoService.searchKudos(any())).thenReturn(pagedResult);

                mockMvc.perform(get("/api/v1/kudos"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].receptor").value("Maria G."));
        }

        @Test
        @DisplayName("GET /api/v1/kudos with empty results should return 200 OK")
        void getKudos_Empty_ReturnsOk() throws Exception {
                var emptyResult = new PagedResult<KudoListItemDTO>(Collections.emptyList(), 0, 0, 0, 10);
                when(kudoService.searchKudos(any())).thenReturn(emptyResult);

                mockMvc.perform(get("/api/v1/kudos"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty());
        }
}
