package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.out.CatalogStorageUnavailableException;
import com.your.summoner.domain.model.Patch;
import com.your.summoner.domain.model.PatchStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatchControllerTest extends RestControllerTestBase {

    @Test
    void patchesEndpointMarksActive() throws Exception {
        when(repository.findAllPatches()).thenReturn(List.of(
                new Patch("16.22.1", false, Map.of("pt_BR", PatchStatus.INGESTING)),
                new Patch("16.21.1", true, Map.of("pt_BR", PatchStatus.READY)),
                new Patch("16.20.1", false, Map.of())));

        mvc.perform(get("/patches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].version", contains("16.22.1", "16.21.1", "16.20.1")))
                .andExpect(jsonPath("$[0].active").value(false))
                .andExpect(jsonPath("$[0].statusByLocale.pt_BR").value("INGESTING"))
                .andExpect(jsonPath("$[1].active").value(true))
                .andExpect(jsonPath("$[1].statusByLocale.pt_BR").value("READY"))
                .andExpect(jsonPath("$[2].active").value(false));
    }

    @Test
    void storageDownIsA503Problem() throws Exception {
        when(repository.findAllPatches()).thenThrow(new CatalogStorageUnavailableException("mongo down", null));

        mvc.perform(get("/patches"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }
}
