package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.application.port.out.LeagueProvider;
import com.your.summoner.application.port.out.MatchProvider;
import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.support.RiotApiWireMock;
import com.your.summoner.support.RiotEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.your.summoner.support.RiotApiWireMock.RIOT;
import static com.your.summoner.support.RiotApiWireMock.riotError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Status HTTP da Riot -> resultado/excecao de dominio, sem retry em 4xx, headers e roteamento por host. */
class RiotAdapterErrorHandlingTest extends RiotApiWireMock {

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



    // ---------- 404 ----------

    @ParameterizedTest
    @EnumSource(RiotEndpoint.class)
    void shouldReturnEmptyWhenRiotReturns404(RiotEndpoint endpoint) {
        // given
        stubError(endpoint, 404, "Data not found");

        // when
        Object result = invoke(endpoint);

        // then
        assertTrue(isEmpty(result), () -> endpoint + " should be empty but was " + result);
        verifyRequests(1, endpoint.path());
    }

    // ---------- 429 ----------

    @ParameterizedTest
    @EnumSource(RiotEndpoint.class)
    void shouldThrowRateLimitWithRetryAfterAndNotRetryOn429(RiotEndpoint endpoint) {
        // given
        wireMock.stubFor(get(urlPathEqualTo(endpoint.path())).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "7")
                .withHeader("Content-Type", "application/json")
                .withBody(riotError(429, "Rate limit exceeded"))));

        // when
        var e = assertThrows(RateLimitExceededException.class, () -> invoke(endpoint));

        // then
        assertEquals(Optional.of(Duration.ofSeconds(7)), e.retryAfter());
        verifyRequests(1, endpoint.path());
    }

    @Test
    void shouldThrowRateLimitWithoutRetryAfterWhenHeaderIsMissing() {
        // given
        stubError(RiotEndpoint.SUMMONER, 429, "Rate limit exceeded");

        // when
        var e = assertThrows(RateLimitExceededException.class, () -> invoke(RiotEndpoint.SUMMONER));

        // then
        assertTrue(e.retryAfter().isEmpty());
        verifyRequests(1, RiotEndpoint.SUMMONER.path());
    }

    // ---------- 401 / 403 / 400 ----------

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldThrowAuthenticationExceptionAndNotRetryOn401And403(int status) {
        // given
        stubError(RiotEndpoint.SUMMONER, status, "Forbidden");

        // when
        var e = assertThrows(ExternalAuthenticationException.class, () -> invoke(RiotEndpoint.SUMMONER));

        // then
        assertTrue(e.getMessage().contains(String.valueOf(status)), e.getMessage());
        verifyRequests(1, RiotEndpoint.SUMMONER.path());
    }

    @ParameterizedTest
    @EnumSource(RiotEndpoint.class)
    void shouldThrowBadResponseAndNotRetryOn400(RiotEndpoint endpoint) {
        // given
        stubError(endpoint, 400, "Bad Request - Unknown apikey");

        // when
        var e = assertThrows(ExternalBadResponseException.class, () -> invoke(endpoint));

        // then
        assertTrue(e.getMessage().contains("Bad Request - Unknown apikey"), e.getMessage());
        verifyRequests(1, endpoint.path());
    }

    // ---------- header ----------

    @ParameterizedTest
    @EnumSource(RiotEndpoint.class)
    void shouldSendRiotTokenHeader(RiotEndpoint endpoint) {
        // when
        Object result = invoke(endpoint);

        // then
        assertFalse(isEmpty(result), () -> endpoint + " should succeed with the default stub");
        wireMock.verify(exactly(1), getRequestedFor(urlPathEqualTo(endpoint.path()))
                .withHeader("X-Riot-Token", equalTo("test-key")));
    }

    // ---------- roteamento de host ----------

    @Test
    void shouldRoutePlatformEndpointsToThePlatformHost() {
        // given
        String kr = RIOT + "/kr";
        RiotApiWireMock.json(wireMock, kr + "/lol/summoner/v4/summoners/by-puuid/" + PUUID, "summoner-tester.json");
        RiotApiWireMock.json(wireMock, kr + "/lol/league/v4/entries/by-puuid/" + PUUID, "league-tester.json");
        RiotApiWireMock.json(wireMock, kr + "/lol/champion-mastery/v4/champion-masteries/by-puuid/" + PUUID, "mastery-tester.json");

        // when
        var summoner = summoners.findByPuuid(PUUID, Platform.KR);
        var league = leagues.findByPuuid(PUUID, Platform.KR);
        var mastery = masteries.findByPuuid(PUUID, Platform.KR);

        // then
        assertTrue(summoner.isPresent());
        assertFalse(league.isEmpty());
        assertFalse(mastery.isEmpty());
        verifyRequests(1, kr + "/lol/summoner/v4/summoners/by-puuid/" + PUUID);
        verifyRequests(1, kr + "/lol/league/v4/entries/by-puuid/" + PUUID);
        verifyRequests(1, kr + "/lol/champion-mastery/v4/champion-masteries/by-puuid/" + PUUID);
        verifyRequests(0, RiotEndpoint.SUMMONER.path());
        verifyRequests(0, RiotEndpoint.LEAGUE.path());
        verifyRequests(0, RiotEndpoint.MASTERY.path());
    }

    @Test
    void shouldRouteBrazilPlatformEndpointsToBr1() {
        // when
        summoners.findByPuuid(PUUID, Platform.BR1);
        leagues.findByPuuid(PUUID, Platform.BR1);
        masteries.findByPuuid(PUUID, Platform.BR1);

        // then
        verifyRequests(1, RiotEndpoint.SUMMONER.path());
        verifyRequests(1, RiotEndpoint.LEAGUE.path());
        verifyRequests(1, RiotEndpoint.MASTERY.path());
    }

    @Test
    void shouldRouteRegionalEndpointsToTheRegionHost() {
        // given
        RiotApiWireMock.json(wireMock, RIOT + "/asia/riot/account/v1/accounts/by-riot-id/Tester/BR1", "account-tester.json");
        RiotApiWireMock.json(wireMock, RIOT + "/asia/lol/match/v5/matches/by-puuid/" + PUUID + "/ids", "match-ids-tester.json");
        RiotApiWireMock.json(wireMock, RIOT + "/asia/lol/match/v5/matches/BR1_3000000001", "match-BR1_3000000001.json");

        // when (KR -> ASIA)
        var account = accounts.findByRiotId(new RiotId("Tester", "BR1"), Platform.KR.accountRegion());
        var ids = matches.findMatchIds(PUUID, Platform.KR.matchRegion(), MatchQuery.empty());
        var match = matches.findById("BR1_3000000001", Platform.KR.matchRegion());

        // then
        assertTrue(account.isPresent());
        assertFalse(ids.isEmpty());
        assertTrue(match.isPresent());
        verifyRequests(1, RIOT + "/asia/riot/account/v1/accounts/by-riot-id/Tester/BR1");
        verifyRequests(1, RIOT + "/asia/lol/match/v5/matches/by-puuid/" + PUUID + "/ids");
        verifyRequests(1, RIOT + "/asia/lol/match/v5/matches/BR1_3000000001");
        verifyRequests(0, RiotEndpoint.ACCOUNT.path());
        verifyRequests(0, RiotEndpoint.MATCH_IDS.path());
        verifyRequests(0, RiotEndpoint.MATCH.path());
    }

    @Test
    void shouldRouteAmericasRegionalEndpointsToAmericas() {
        // when
        var account = accounts.findByRiotId(new RiotId("Tester", "BR1"), Platform.BR1.accountRegion());
        var ids = matches.findMatchIds(PUUID, Platform.BR1.matchRegion(), MatchQuery.empty());

        // then
        assertTrue(account.isPresent());
        assertFalse(ids.isEmpty());
        verifyRequests(1, RiotEndpoint.ACCOUNT.path());
        verifyRequests(1, RiotEndpoint.MATCH_IDS.path());
    }

    @Test
    void shouldRouteSeaPlatformToSeaForMatchesButToAsiaForAccounts() {
        // given: account-v1 nao atende em SEA, entao OC1 usa ASIA na conta e SEA nas partidas
        RiotApiWireMock.json(wireMock, RIOT + "/asia/riot/account/v1/accounts/by-riot-id/Tester/BR1", "account-tester.json");
        RiotApiWireMock.json(wireMock, RIOT + "/sea/lol/match/v5/matches/BR1_3000000001", "match-BR1_3000000001.json");

        // when
        var account = accounts.findByRiotId(new RiotId("Tester", "BR1"), Platform.OC1.accountRegion());
        var match = matches.findById("BR1_3000000001", Platform.OC1.matchRegion());

        // then
        assertEquals(Region.ASIA, Platform.OC1.accountRegion());
        assertEquals(Region.SEA, Platform.OC1.matchRegion());
        assertTrue(account.isPresent());
        assertTrue(match.isPresent());
        verifyRequests(1, RIOT + "/asia/riot/account/v1/accounts/by-riot-id/Tester/BR1");
        verifyRequests(1, RIOT + "/sea/lol/match/v5/matches/BR1_3000000001");
    }

    // ---------- encoding ----------

    @Test
    void shouldUrlEncodeNicknamesWithSpaceAndAccents() {
        // given
        String encoded = RIOT + "/americas/riot/account/v1/accounts/by-riot-id/J%C3%A3o%20Silva/BR1";
        RiotApiWireMock.json(wireMock, encoded, "account-tester.json");

        // when
        var account = accounts.findByRiotId(new RiotId("Jão Silva", "BR1"), Region.AMERICAS);

        // then
        assertTrue(account.isPresent(), "the encoded path should have matched the stub");
        verifyRequests(1, encoded);
    }

    @Test
    void requestsCarryTestKeyOnly() {
        for (RiotEndpoint endpoint : RiotEndpoint.values()) {
            invoke(endpoint);
        }
        assertEquals(6, wireMock.getAllServeEvents().size());
        wireMock.getAllServeEvents().forEach(event ->
                assertEquals(java.util.List.of("test-key"),
                        event.getRequest().getHeaders().getHeader("X-Riot-Token").values()));
    }

    // ---------- helpers ----------

    private Object invoke(RiotEndpoint endpoint) {
        return switch (endpoint) {
            case SUMMONER -> summoners.findByPuuid(PUUID, Platform.BR1);
            case ACCOUNT -> accounts.findByRiotId(new RiotId("Tester", "BR1"), Region.AMERICAS);
            case LEAGUE -> leagues.findByPuuid(PUUID, Platform.BR1);
            case MASTERY -> masteries.findByPuuid(PUUID, Platform.BR1);
            case MATCH_IDS -> matches.findMatchIds(PUUID, Region.AMERICAS, MatchQuery.empty());
            case MATCH -> matches.findById("BR1_3000000001", Region.AMERICAS);
        };
    }

    private static boolean isEmpty(Object result) {
        if (result instanceof Optional<?> o) {
            return o.isEmpty();
        }
        return assertInstanceOf(Collection.class, result).isEmpty();
    }

    private void stubError(RiotEndpoint endpoint, int status, String message) {
        wireMock.stubFor(get(urlPathEqualTo(endpoint.path())).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(riotError(status, message))));
    }

    private void verifyRequests(int count, String path) {
        wireMock.verify(exactly(count), getRequestedFor(urlPathEqualTo(path)));
    }
}
