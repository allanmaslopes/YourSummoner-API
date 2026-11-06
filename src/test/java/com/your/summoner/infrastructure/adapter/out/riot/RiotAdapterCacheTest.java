package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.application.port.out.LeagueProvider;
import com.your.summoner.application.port.out.MatchProvider;
import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.exception.ExternalServiceException;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.support.RiotApiWireMock;
import com.your.summoner.support.RiotEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.your.summoner.support.RiotApiWireMock.RIOT;
import static com.your.summoner.support.RiotApiWireMock.riotError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cache real Caffeine, isolado entre casos pelo suporte compartilhado. */
class RiotAdapterCacheTest extends RiotApiWireMock {

    private static final String PUUID = "puuid-tester";

    @org.springframework.beans.factory.annotation.Autowired
    SummonerProvider summoners;
    @org.springframework.beans.factory.annotation.Autowired
    AccountProvider accounts;
    @org.springframework.beans.factory.annotation.Autowired
    LeagueProvider leagues;
    @org.springframework.beans.factory.annotation.Autowired
    ChampionMasteryProvider masteries;
    @org.springframework.beans.factory.annotation.Autowired
    MatchProvider matches;

    @ParameterizedTest
    @EnumSource(RiotEndpoint.class)
    void shouldHitRiotOnlyOnceForRepeatedCalls(RiotEndpoint endpoint) {
        // when
        Object first = invoke(endpoint, Platform.BR1);
        Object second = invoke(endpoint, Platform.BR1);

        // then
        assertEquals(first, second);
        verifyRequests(1, endpoint.path());
    }

    @Test
    void shouldNotShareSummonerCacheEntriesAcrossPlatforms() {
        // given
        stubSummoner(Platform.KR);

        // when
        summoners.findByPuuid(PUUID, Platform.BR1);
        summoners.findByPuuid(PUUID, Platform.KR);
        summoners.findByPuuid(PUUID, Platform.BR1);
        summoners.findByPuuid(PUUID, Platform.KR);

        // then
        verifyRequests(1, RiotEndpoint.SUMMONER.path());
        verifyRequests(1, summonerPath(Platform.KR));
    }

    @Test
    void shouldNotShareLeagueAndMasteryCacheEntriesAcrossPlatforms() {
        // given
        RiotApiWireMock.json(wireMock, RIOT + "/kr/lol/league/v4/entries/by-puuid/" + PUUID, "league-tester.json");
        RiotApiWireMock.json(wireMock, RIOT + "/kr/lol/champion-mastery/v4/champion-masteries/by-puuid/" + PUUID,
                "mastery-tester.json");

        // when
        leagues.findByPuuid(PUUID, Platform.BR1);
        leagues.findByPuuid(PUUID, Platform.KR);
        masteries.findByPuuid(PUUID, Platform.BR1);
        masteries.findByPuuid(PUUID, Platform.KR);

        // then
        verifyRequests(1, RiotEndpoint.LEAGUE.path());
        verifyRequests(1, RIOT + "/kr/lol/league/v4/entries/by-puuid/" + PUUID);
        verifyRequests(1, RiotEndpoint.MASTERY.path());
        verifyRequests(1, RIOT + "/kr/lol/champion-mastery/v4/champion-masteries/by-puuid/" + PUUID);
    }

    @Test
    void shouldNotShareAccountCacheEntriesAcrossRegions() {
        // given
        RiotApiWireMock.json(wireMock, RIOT + "/europe/riot/account/v1/accounts/by-riot-id/Tester/BR1", "account-tester.json");
        var riotId = new RiotId("Tester", "BR1");

        // when
        accounts.findByRiotId(riotId, Region.AMERICAS);
        accounts.findByRiotId(riotId, Region.EUROPE);
        accounts.findByRiotId(riotId, Region.AMERICAS);
        accounts.findByRiotId(riotId, Region.EUROPE);

        // then
        verifyRequests(1, RiotEndpoint.ACCOUNT.path());
        verifyRequests(1, RIOT + "/europe/riot/account/v1/accounts/by-riot-id/Tester/BR1");
    }

    @Test
    void shouldShareAccountCacheEntryRegardlessOfRiotIdCase() {
        // given: stub case-insensitive para a mesma conta
        wireMock.stubFor(get(urlPathMatching("(?i)" + RIOT + "/americas/riot/account/v1/accounts/by-riot-id/ghost/br1"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(RiotApiWireMock.body("account-tester.json"))));

        // when
        accounts.findByRiotId(new RiotId("Ghost", "BR1"), Region.AMERICAS);
        accounts.findByRiotId(new RiotId("ghost", "br1"), Region.AMERICAS);

        // then
        wireMock.verify(exactly(1), getRequestedFor(urlPathMatching("(?i)" + RIOT
                + "/americas/riot/account/v1/accounts/by-riot-id/ghost/br1")));
    }

    @Test
    void shouldNotShareMatchCacheEntriesAcrossRegions() {
        // given
        RiotApiWireMock.json(wireMock, RIOT + "/europe/lol/match/v5/matches/BR1_3000000001", "match-BR1_3000000001.json");
        RiotApiWireMock.json(wireMock, RIOT + "/europe/lol/match/v5/matches/by-puuid/" + PUUID + "/ids", "match-ids-tester.json");

        // when
        matches.findById("BR1_3000000001", Region.AMERICAS);
        matches.findById("BR1_3000000001", Region.EUROPE);
        matches.findMatchIds(PUUID, Region.AMERICAS, MatchQuery.empty());
        matches.findMatchIds(PUUID, Region.EUROPE, MatchQuery.empty());

        // then
        verifyRequests(1, RiotEndpoint.MATCH.path());
        verifyRequests(1, RIOT + "/europe/lol/match/v5/matches/BR1_3000000001");
        verifyRequests(1, RiotEndpoint.MATCH_IDS.path());
        verifyRequests(1, RIOT + "/europe/lol/match/v5/matches/by-puuid/" + PUUID + "/ids");
    }

    @Test
    void shouldCacheMatchIdsPerQuery() {
        // when
        matches.findMatchIds(PUUID, Region.AMERICAS, MatchQuery.empty());
        matches.findMatchIds(PUUID, Region.AMERICAS, new MatchQuery(null, null, null, null, 0, 5));
        matches.findMatchIds(PUUID, Region.AMERICAS, new MatchQuery(null, null, null, null, 0, 5));

        // then
        verifyRequests(2, RiotEndpoint.MATCH_IDS.path());
    }

    // 404 não é cacheado (ADR 0001 c).
    @ParameterizedTest
    @EnumSource(RiotEndpoint.class)
    void shouldNotCacheNotFoundResults(RiotEndpoint endpoint) {
        // given
        wireMock.stubFor(get(urlPathEqualTo(endpoint.path())).willReturn(aResponse().withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(riotError(404, "Data not found"))));

        // when
        invoke(endpoint, Platform.BR1);
        invoke(endpoint, Platform.BR1);

        // then
        verifyRequests(2, endpoint.path());
    }

    @Test
    void shouldNotCacheA503AndFetchAgainOnNextCall() {
        // given: 503 em todas as tentativas (esgota o retry)
        wireMock.stubFor(get(urlPathEqualTo(RiotEndpoint.SUMMONER.path())).willReturn(aResponse().withStatus(503)));
        assertThrows(ExternalServiceException.class, () -> summoners.findByPuuid(PUUID, Platform.BR1));

        // when: upstream volta
        stubSummoner(Platform.BR1);
        var result = summoners.findByPuuid(PUUID, Platform.BR1);

        // then: buscou de novo, o erro não ficou no cache
        assertTrue(result.isPresent());
        wireMock.verify(com.github.tomakehurst.wiremock.client.WireMock.moreThan(1),
                getRequestedFor(urlPathEqualTo(RiotEndpoint.SUMMONER.path())));
    }

    // ---------- helpers ----------

    private Object invoke(RiotEndpoint endpoint, Platform platform) {
        return switch (endpoint) {
            case SUMMONER -> summoners.findByPuuid(PUUID, platform);
            case ACCOUNT -> accounts.findByRiotId(new RiotId("Tester", "BR1"), platform.accountRegion());
            case LEAGUE -> leagues.findByPuuid(PUUID, platform);
            case MASTERY -> masteries.findByPuuid(PUUID, platform);
            case MATCH_IDS -> matches.findMatchIds(PUUID, platform.matchRegion(), MatchQuery.empty());
            case MATCH -> matches.findById("BR1_3000000001", platform.matchRegion());
        };
    }

    private static String summonerPath(Platform platform) {
        return RIOT + "/" + platform.host() + "/lol/summoner/v4/summoners/by-puuid/" + PUUID;
    }

    private void stubSummoner(Platform platform) {
        RiotApiWireMock.json(wireMock, summonerPath(platform), "summoner-tester.json");
    }

    private void verifyRequests(int count, String path) {
        wireMock.verify(exactly(count), getRequestedFor(urlPathEqualTo(path)));
    }
}
