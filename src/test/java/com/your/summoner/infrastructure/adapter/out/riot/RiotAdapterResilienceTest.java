package com.your.summoner.infrastructure.adapter.out.riot;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.application.port.out.MatchProvider;
import com.your.summoner.domain.exception.ExternalAuthenticationException;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.application.port.out.LeagueProvider;
import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.support.RiotApiWireMock;
import com.your.summoner.support.RiotEndpoint;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.your.summoner.support.RiotApiWireMock.riotError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Retry, timeout e circuit breaker com janela de quatro tentativas HTTP.
 * O suporte compartilhado limpa caches, circuitos e retries entre todos os casos.
 */
class RiotAdapterResilienceTest extends RiotApiWireMock {

    private static final String PUUID = "puuid-tester";
    private static final int CB_THRESHOLD = 4;


    @org.springframework.beans.factory.annotation.Autowired
    SummonerProvider summoners;
    @org.springframework.beans.factory.annotation.Autowired
    LeagueProvider leagues;
    @org.springframework.beans.factory.annotation.Autowired
    ChampionMasteryProvider masteries;
    @org.springframework.beans.factory.annotation.Autowired
    MatchProvider matches;
    @org.springframework.beans.factory.annotation.Autowired
    AccountProvider accounts;



    // ---------- retry ----------

    @Test
    void shouldSucceedOnSecondAttemptWhenRiotReturns503ThenOk() {
        // given
        String path = RiotEndpoint.SUMMONER.path();
        wireMock.stubFor(get(urlPathEqualTo(path)).inScenario("flaky")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503).withBody(riotError(503, "Service unavailable")))
                .willSetStateTo("recovered"));
        wireMock.stubFor(get(urlPathEqualTo(path)).inScenario("flaky")
                .whenScenarioStateIs("recovered")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(RiotApiWireMock.body("summoner-tester.json"))));

        // when
        var summoner = summoners.findByPuuid(PUUID, Platform.BR1);

        // then
        assertTrue(summoner.isPresent());
        assertEquals(PUUID, summoner.get().puuid());
        verifyRequests(2, path);
    }

    @Test
    void shouldGiveUpAfterThreeAttemptsWhenRiotKeepsReturning503() {
        // given
        String path = RiotEndpoint.SUMMONER.path();
        wireMock.stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse().withStatus(503).withBody(riotError(503, "Service unavailable"))));

        // when
        var e = assertThrows(ExternalServiceUnavailableException.class,
                () -> summoners.findByPuuid(PUUID, Platform.BR1));

        // then: 1 chamada + 2 retries
        assertTrue(e.getMessage().contains("503"), e.getMessage());
        verifyRequests(3, path);
    }

    // ---------- timeout ----------

    @Test
    void shouldThrowUnavailableWhenRiotRespondsSlowerThanTheReadTimeout() {
        // given: read-timeout do contexto de teste = 300ms
        String path = RiotEndpoint.SUMMONER.path();
        wireMock.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(RiotApiWireMock.body("summoner-tester.json"))
                .withFixedDelay(1500)));

        // when
        var e = assertThrows(ExternalServiceUnavailableException.class,
                () -> summoners.findByPuuid(PUUID, Platform.BR1));

        // then: timeout e transitorio, entao tambem foi tentado de novo (1 + 2). O WireMock so grava a
        // requisicao ao fim do atraso, por isso a contagem e aguardada.
        assertTrue(e.getMessage().contains("Could not reach"), e.getMessage());
        awaitRequests(3, path);
    }

    // ---------- circuit breaker ----------

    @Test
    void shouldOpenCircuitAfterRepeatedFailuresAndStopCallingRiot() {
        // given
        String path = RiotEndpoint.LEAGUE.path();
        wireMock.stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse().withStatus(503).withBody(riotError(503, "Service unavailable"))));

        // when: falhas ate o circuito abrir (a janela de CB_THRESHOLD chamadas enche)
        for (int i = 0; i < CB_THRESHOLD; i++) {
            try {
                leagues.findByPuuid(PUUID, Platform.BR1);
                fail("503 should never succeed");
            } catch (ExternalServiceUnavailableException | CallNotPermittedException expected) {
                // esperado
            }
        }
        int requestsUntilOpen = wireMock.countRequestsMatching(
                getRequestedFor(urlPathEqualTo(path)).build()).getCount();

        // then: a proxima chamada falha rapido, sem tocar na Riot
        assertThrows(CallNotPermittedException.class, () -> leagues.findByPuuid(PUUID, Platform.BR1));
        assertEquals(CB_THRESHOLD, requestsUntilOpen, "the circuit should open exactly when the window fills");
        verifyRequests(requestsUntilOpen, path);
    }

    @Test
    void shouldNotOpenCircuitOnRateLimit() {
        // given
        String path = RiotEndpoint.MASTERY.path();
        wireMock.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "1")
                .withBody(riotError(429, "Rate limit exceeded"))));
        int calls = CB_THRESHOLD * 3;

        // when
        for (int i = 0; i < calls; i++) {
            assertThrows(RateLimitExceededException.class, () -> masteries.findByPuuid(PUUID, Platform.BR1));
        }

        // then: todas chegaram na Riot; nenhuma virou CallNotPermittedException
        verifyRequests(calls, path);
    }

    @Test
    void shouldNotOpenCircuitOnRepeatedUnauthorized() {
        // given: 401 e erro de credencial, nao de disponibilidade (fora do failOn)
        String path = RiotEndpoint.MATCH.path();
        wireMock.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(401)
                .withBody(riotError(401, "Unknown apikey"))));
        int calls = CB_THRESHOLD * 3;

        // when / then
        for (int i = 0; i < calls; i++) {
            assertThrows(ExternalAuthenticationException.class,
                    () -> matches.findById("BR1_3000000001", Region.AMERICAS));
        }
        verifyRequests(calls, path);
    }

    @Test
    void shouldNotOpenCircuitOnRepeatedBadRequest() {
        // given
        String path = RiotEndpoint.ACCOUNT.path();
        wireMock.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(400)
                .withBody(riotError(400, "Bad Request"))));
        int calls = CB_THRESHOLD * 3;

        // when / then
        for (int i = 0; i < calls; i++) {
            assertThrows(ExternalBadResponseException.class,
                    () -> accounts.findByRiotId(new RiotId("Tester", "BR1"), Region.AMERICAS));
        }
        verifyRequests(calls, path);
    }

    // ---------- helpers ----------

    private void verifyRequests(int count, String path) {
        wireMock.verify(exactly(count), getRequestedFor(urlPathEqualTo(path)));
    }

    /** Aguarda (sem sleep fixo) o journal do WireMock registrar {@code count} requisicoes. */
    private void awaitRequests(int count, String path) {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        int seen;
        while ((seen = wireMock.countRequestsMatching(getRequestedFor(urlPathEqualTo(path)).build()).getCount()) < count
                && System.nanoTime() < deadline) {
            LockSupport.parkNanos(Duration.ofMillis(50).toNanos());
        }
        assertEquals(count, seen, "requests recorded by WireMock for " + path);
    }
}
