package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.LeagueEntry;
import com.your.summoner.domain.model.MainChampion;
import com.your.summoner.domain.model.MasteredChampion;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.domain.model.SummonerDetails;
import com.your.summoner.domain.model.SummonerProfile;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SummonerControllerTest extends RestControllerTestBase {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final RiotId TESTER = new RiotId("Tester", "BR1");

    @Test
    void returnsTheAggregatedProfile() throws Exception {
        var lee = new MasteredChampion(new Champion("LeeSin", 64, "Lee Sin", "", List.of("Fighter", "Assassin")),
                new ChampionMastery(64, 30, 250000, 0), null);
        when(getProfile.execute(TESTER, Platform.BR1)).thenReturn(new SummonerProfile(
                new Account("puuid-tester", "Tester", "BR1"),
                new SummonerDetails("puuid-tester", 4568, 150, 0),
                "https://cdn/16.21.1/img/profileicon/4568.png",
                List.of(new LeagueEntry("RANKED_SOLO_5x5", "GOLD", "II", 40, 55, 45, true)),
                List.of(new MainChampion(lee, "https://cdn/16.21.1/img/champion/LeeSin.png"))));

        mvc.perform(get("/summoners/Tester/BR1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summonerName").value("Tester"))
                .andExpect(jsonPath("$.tagLine").value("BR1"))
                .andExpect(jsonPath("$.puuid").value("puuid-tester"))
                .andExpect(jsonPath("$.level").value(150))
                .andExpect(jsonPath("$.urlIcon", endsWith("/img/profileicon/4568.png")))
                .andExpect(jsonPath("$.rankingList", hasSize(1)))
                .andExpect(jsonPath("$.rankingList[0].queueType").value("RANKED_SOLO_5x5"))
                .andExpect(jsonPath("$.rankingList[0].tier").value("Gold"))
                .andExpect(jsonPath("$.rankingList[0].rank").value("II"))
                .andExpect(jsonPath("$.rankingList[0].leaguePoints").value(40))
                .andExpect(jsonPath("$.rankingList[0].quantMatchs").value(100))
                .andExpect(jsonPath("$.rankingList[0].winRate").value(55.0))
                .andExpect(jsonPath("$.rankingList[0].hotStreak").value(true))
                .andExpect(jsonPath("$.mainChampionList", hasSize(1)))
                .andExpect(jsonPath("$.mainChampionList[0].id").value("LeeSin"))
                .andExpect(jsonPath("$.mainChampionList[0].key").value(64))
                .andExpect(jsonPath("$.mainChampionList[0].championName").value("Lee Sin"))
                .andExpect(jsonPath("$.mainChampionList[0].masteryPoints").value(250000))
                .andExpect(jsonPath("$.mainChampionList[0].masteryLevel").value(30))
                .andExpect(jsonPath("$.mainChampionList[0].urlIcon", endsWith("/img/champion/LeeSin.png")))
                .andExpect(jsonPath("$.mainChampionList[0].tags", contains("Fighter", "Assassin")));
    }

    @Test
    void routesTheRequestedPlatform() throws Exception {
        when(getProfile.execute(new RiotId("Hide on bush", "KR1"), Platform.KR)).thenReturn(new SummonerProfile(
                new Account("p", "Hide on bush", "KR1"), new SummonerDetails("p", 6, 946, 0), "icon",
                List.of(), List.of()));

        mvc.perform(get("/summoners/Hide on bush/KR1").param("platform", "kr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(946))
                .andExpect(jsonPath("$.rankingList", hasSize(0)))
                .andExpect(jsonPath("$.mainChampionList", hasSize(0)));
    }

    @Test
    void unknownRiotIdIsA404Problem() throws Exception {
        when(getProfile.execute(any(), any())).thenThrow(new ResourceNotFoundException("Summoner", "Ghost#BR1"));

        mvc.perform(get("/summoners/Ghost/BR1"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Summoner not found"))
                .andExpect(jsonPath("$.identifier").value("Ghost#BR1"));
    }

    @Test
    void unknownPlatformIsA400Problem() throws Exception {
        mvc.perform(get("/summoners/Tester/BR1").param("platform", "XX9"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", containsString("Unknown platform")));

        verifyNoInteractions(getProfile);
    }

    @Test
    void rateLimitIsPropagatedWithRetryAfter() throws Exception {
        when(getProfile.execute(any(), any())).thenThrow(new RateLimitExceededException(Duration.ofSeconds(7)));

        mvc.perform(get("/summoners/Limited/BR1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "7"))
                .andExpect(jsonPath("$.title").value("Riot API rate limit exceeded"));
    }

    @Test
    void rejectedApiKeyIsA502() throws Exception {
        when(getProfile.execute(any(), any())).thenThrow(new ExternalAuthenticationException("Riot API", "403"));

        mvc.perform(get("/summoners/Forbidden/BR1"))
                .andExpect(status().isBadGateway())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Upstream rejected the service credentials"));
    }
}
