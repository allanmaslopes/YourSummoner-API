package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.PlayerMatch;
import com.your.summoner.domain.model.RiotId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchControllerTest extends RestControllerTestBase {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final RiotId TESTER = new RiotId("Tester", "BR1");

    private static final Match.Participant LEE = new Match.Participant("puuid-tester", "Tester", "BR1", 100, 64,
            "LeeSin", "JUNGLE", 16, 8, 3, 10, 40, 120, 11000, 25000, 30, true,
            List.of(3071, 3158, 0, 0, 0, 0, 3340), 4, 11, 0, 0);
    private static final Match MATCH = new Match("BR1_3000000001", "BR1", 1700000000000L, 1834, "CLASSIC", "MATCHED_GAME",
            "16.21.1", 420, 11, List.of(LEE),
            List.of(new Match.Team(100, true, List.of(222), 1, 3, 7, 25)));

    @Test
    void listsMatchIdsForwardingFilters() throws Exception {
        when(listMatchIds.execute(eq(TESTER), eq(Platform.BR1), any()))
                .thenReturn(List.of("BR1_3000000002", "BR1_3000000001"));

        mvc.perform(get("/matchs/Tester/BR1").param("quantity", "2").param("type", "RANKED").param("queue", "420"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", contains("BR1_3000000002", "BR1_3000000001")));

        var query = ArgumentCaptor.forClass(MatchQuery.class);
        verify(listMatchIds).execute(eq(TESTER), eq(Platform.BR1), query.capture());
        assertEquals(new MatchQuery(null, null, 420, "ranked", null, 2), query.getValue());
    }

    @Test
    void invalidQuantityIsA400Problem() throws Exception {
        mvc.perform(get("/matchs/Tester/BR1").param("quantity", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));

        verifyNoInteractions(listMatchIds);
    }

    @Test
    void nonNumericQuantityIsA400Problem() throws Exception {
        mvc.perform(get("/matchs/Tester/BR1").param("quantity", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));
    }

    @Test
    void returnsMatchDetail() throws Exception {
        when(getMatch.execute("BR1_3000000001")).thenReturn(MATCH);

        mvc.perform(get("/matchs/matchId/BR1_3000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value("BR1_3000000001"))
                .andExpect(jsonPath("$.platformId").value("BR1"))
                .andExpect(jsonPath("$.queueId").value(420))
                .andExpect(jsonPath("$.gameDuration").value(1834))
                .andExpect(jsonPath("$.participants", hasSize(1)))
                .andExpect(jsonPath("$.participants[0].championName").value("LeeSin"))
                .andExpect(jsonPath("$.participants[0].kda").value(6.0))
                .andExpect(jsonPath("$.participants[0].items", hasSize(7)))
                .andExpect(jsonPath("$.participants[0].playerSubteamId").value(0))
                .andExpect(jsonPath("$.participants[0].riotIdTagline").value("BR1"))
                .andExpect(jsonPath("$.teams[0].bannedChampionIds", contains(222)))
                .andExpect(jsonPath("$.teams[0].dragonKills").value(3));
    }

    @Test
    void unknownMatchIsA404Problem() throws Exception {
        when(getMatch.execute("BR1_404")).thenThrow(new ResourceNotFoundException("Match", "BR1_404"));

        mvc.perform(get("/matchs/matchId/BR1_404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Match not found"));
    }

    @Test
    void malformedMatchIdIsA400() throws Exception {
        when(getMatch.execute("abc")).thenThrow(new InvalidInputException("Invalid match id 'abc'"));

        mvc.perform(get("/matchs/matchId/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));
    }

    @Test
    void historyReturnsThePlayersPerformance() throws Exception {
        when(getHistory.history(eq(TESTER), eq(Platform.BR1), any())).thenReturn(List.of(new PlayerMatch(MATCH, LEE)));

        mvc.perform(get("/matchs/Tester/BR1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].matchId").value("BR1_3000000001"))
                .andExpect(jsonPath("$[0].championName").value("LeeSin"))
                .andExpect(jsonPath("$[0].kills").value(8))
                .andExpect(jsonPath("$[0].deaths").value(3))
                .andExpect(jsonPath("$[0].assists").value(10))
                .andExpect(jsonPath("$[0].kda").value(6.0))
                .andExpect(jsonPath("$[0].win").value(true))
                .andExpect(jsonPath("$[0].queueId").value(420))
                .andExpect(jsonPath("$[0].gameMode").value("CLASSIC"))
                .andExpect(jsonPath("$[0].items", hasSize(7)));
    }

    @Test
    void historyDefaultsToTwentyMatches() throws Exception {
        when(getHistory.history(any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/matchs/Tester/BR1/history")).andExpect(status().isOk());

        var query = ArgumentCaptor.forClass(MatchQuery.class);
        verify(getHistory).history(eq(TESTER), eq(Platform.BR1), query.capture());
        assertEquals(20, query.getValue().count());
    }

    @Test
    void historyQuantityOutOfRangeIsA400WithoutCallingTheUseCase() throws Exception {
        for (String bad : new String[]{"0", "101"}) {
            mvc.perform(get("/matchs/Tester/BR1/history").param("quantity", bad))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", PROBLEM_JSON));
        }

        verifyNoInteractions(getHistory);
    }

    @Test
    void historyPropagatesRateLimitWithRetryAfter() throws Exception {
        when(getHistory.history(any(), any(), any())).thenThrow(new RateLimitExceededException(Duration.ofSeconds(7)));

        mvc.perform(get("/matchs/Tester/BR1/history"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "7"));
    }
}
