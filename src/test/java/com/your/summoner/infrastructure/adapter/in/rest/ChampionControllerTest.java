package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.MasteredChampion;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChampionControllerTest extends RestControllerTestBase {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final RiotId TESTER = new RiotId("Tester", "BR1");

    @Test
    void returnsTheActivePatch() throws Exception {
        when(getVersion.execute()).thenReturn("16.21.1");

        mvc.perform(get("/champions/findVersion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("16.21.1"));
    }

    @Test
    void returnsChampionDetailAndForwardsThePatch() throws Exception {
        var detail = new ChampionDetail("Ahri", 103, "Ahri", "a Raposa", "lore", "blurb", List.of("Mage"), "Mana",
                new ChampionDetail.Info(3, 4, 8, 5), new ChampionDetail.Passive("Furtividade", "desc"),
                List.of(new ChampionDetail.Spell("AhriQ", "Orbe", "d", "7", "55", "880")),
                List.of(new ChampionDetail.Skin("103000", 0, "default"), new ChampionDetail.Skin("103001", 1, "Dinastia")),
                List.of("dica"), List.of(), "https://cdn/16.21.1/img/champion/Ahri.png");
        when(getDetail.execute("ahri", "16.20.1")).thenReturn(detail);

        mvc.perform(get("/champions/find-champion/ahri").param("patch", "16.20.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("Ahri"))
                .andExpect(jsonPath("$.key").value(103))
                .andExpect(jsonPath("$.info.magic").value(8))
                .andExpect(jsonPath("$.passive.name").value("Furtividade"))
                .andExpect(jsonPath("$.spells[0].id").value("AhriQ"))
                .andExpect(jsonPath("$.spells[0].cooldown").value("7"))
                .andExpect(jsonPath("$.skins", hasSize(2)))
                .andExpect(jsonPath("$.skins[1].name").value("Dinastia"))
                .andExpect(jsonPath("$.allyTips", hasSize(1)))
                .andExpect(jsonPath("$.urlIcon", endsWith("/img/champion/Ahri.png")))
                .andExpect(jsonPath("$.iconUrl").doesNotExist());
    }

    @Test
    void championWithoutPatchPassesNull() throws Exception {
        when(getDetail.execute("Wukong", null)).thenReturn(new ChampionDetail("MonkeyKing", 62, "Wukong", "", "", "",
                List.of(), "Mana", null, null, List.of(), List.of(), List.of(), List.of(), "url"));

        mvc.perform(get("/champions/find-champion/Wukong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MonkeyKing"));
    }

    @Test
    void unknownChampionIsA404Problem() throws Exception {
        when(getDetail.execute(eq("Nope"), any())).thenThrow(new ResourceNotFoundException("Champion", "Nope"));

        mvc.perform(get("/champions/find-champion/Nope"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Champion not found"));
    }

    @Test
    void unknownPatchReturns404Problem() throws Exception {
        when(getDetail.execute("Ahri", "99.99.1")).thenThrow(new ResourceNotFoundException("Patch", "99.99.1"));

        mvc.perform(get("/champions/find-champion/Ahri").param("patch", "99.99.1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Patch not found"))
                .andExpect(jsonPath("$.identifier").value("99.99.1"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void skinsEndpointReturnsSplashAndLoading() throws Exception {
        when(listSkins.execute("kai'sa", "16.21.1")).thenReturn(List.of(
                new ChampionSkin("Kaisa", "145001", 1, "Kai'Sa Arcade", "https://cdn/splash/Kaisa_1.jpg",
                        "https://cdn/loading/Kaisa_1.jpg")));

        mvc.perform(get("/champions/kai'sa/skins").param("patch", "16.21.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].championId").value("Kaisa"))
                .andExpect(jsonPath("$[0].skinId").value("145001"))
                .andExpect(jsonPath("$[0].num").value(1))
                .andExpect(jsonPath("$[0].name").value("Kai'Sa Arcade"))
                .andExpect(jsonPath("$[0].splashUrl").value("https://cdn/splash/Kaisa_1.jpg"))
                .andExpect(jsonPath("$[0].loadingUrl").value("https://cdn/loading/Kaisa_1.jpg"));
    }

    @Test
    void listsMasteriesSortedAsReturnedByTheUseCase() throws Exception {
        when(listMasteries.execute(TESTER, Platform.BR1, null)).thenReturn(List.of(
                mastered("LeeSin", 64, "Lee Sin", 30, 250000, null),
                mastered("Ahri", 103, "Ahri", 12, 90000, null)));

        mvc.perform(get("/champions/by-mastery/Tester/BR1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", contains("LeeSin", "Ahri")))
                .andExpect(jsonPath("$[0].championName").value("Lee Sin"))
                .andExpect(jsonPath("$[0].masteryLevel").value(30))
                .andExpect(jsonPath("$[0].masteryPoints").value(250000))
                .andExpect(jsonPath("$[0].pickRate", nullValue()))
                .andExpect(jsonPath("$[0].tags", contains("Fighter")));
    }

    @Test
    void masteriesForwardPlatformAndPatch() throws Exception {
        when(listMasteries.execute(TESTER, Platform.KR, "16.20.1")).thenReturn(List.of());

        mvc.perform(get("/champions/by-mastery/Tester/BR1").param("platform", "kr").param("patch", "16.20.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void filtersMasteriesByLaneWithPickRate() throws Exception {
        when(listLaneChampions.execute(TESTER, Platform.BR1, Lane.JUNGLE, null)).thenReturn(List.of(
                mastered("LeeSin", 64, "Lee Sin", 30, 250000, 6.381)));
        when(listLaneChampions.execute(TESTER, Platform.BR1, Lane.MIDDLE, null)).thenReturn(List.of(
                mastered("Ahri", 103, "Ahri", 12, 90000, 11.0)));

        mvc.perform(get("/champions/summoner/Tester/tag/BR1/lane/jungle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("LeeSin"))
                .andExpect(jsonPath("$[0].pickRate").value(6.381));

        mvc.perform(get("/champions/summoner/Tester/tag/BR1/lane/mid"))
                .andExpect(jsonPath("$[*].id", contains("Ahri")));
    }

    @Test
    void invalidLaneReturns400Problem() throws Exception {
        mvc.perform(get("/champions/summoner/Tester/tag/BR1/lane/river"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("Unknown lane")));

        verifyNoInteractions(listLaneChampions);
    }

    @Test
    void rateLimitReturns429WithRetryAfter() throws Exception {
        when(listMasteries.execute(any(), any(), any())).thenThrow(new RateLimitExceededException(Duration.ofSeconds(7)));

        mvc.perform(get("/champions/by-mastery/Limited/BR1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "7"))
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Riot API rate limit exceeded"));
    }

    @Test
    void upstreamUnavailableIsA503WithoutInternalDetails() throws Exception {
        when(getVersion.execute()).thenThrow(new ExternalServiceUnavailableException("Data Dragon",
                "Could not reach Data Dragon: connect timed out to localhost:1234"));

        mvc.perform(get("/champions/findVersion"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Data Dragon is temporarily unavailable"))
                .andExpect(jsonPath("$.detail", not(containsString("localhost"))));
    }

    @Test
    void unexpectedFailureIsAGeneric500WithoutLeakingTheMessage() throws Exception {
        when(getVersion.execute()).thenThrow(new IllegalStateException("secret internal state"));

        mvc.perform(get("/champions/findVersion"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(content().string(not(containsString("secret internal state"))))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void allowsTheFrontendOriginsOnGetOnly() throws Exception {
        mvc.perform(options("/champions/findVersion")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));

        mvc.perform(options("/champions/findVersion")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());

        mvc.perform(options("/champions/findVersion")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    private static MasteredChampion mastered(String id, long key, String name, int level, long points, Double pickRate) {
        return new MasteredChampion(new Champion(id, key, name, "", List.of("Fighter")),
                new ChampionMastery(key, level, points, 0), pickRate);
    }
}
