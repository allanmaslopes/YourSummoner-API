package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.support.RiotApiWireMock;
import com.your.summoner.support.RiotEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prazo total de 400 ms, leitura de 300 ms: a 1a tentativa termina em ~300 ms, a 2a em ~610 ms e a 3a
 * (que terminaria em ~920 ms) nao pode comecar, pois o prazo ja estourou.
 */
@TestPropertySource(properties = "riot.resilience.total-deadline=400ms")
class RiotTotalDeadlineTest extends RiotApiWireMock {

    @Autowired
    SummonerProvider summoners;
    @Autowired
    RiotResilience riotResilience;

    @Test
    void riotCallGivesUpAtTotalDeadline() {
        String path = RiotEndpoint.SUMMONER.path();
        wireMock.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200).withFixedDelay(2000)));

        long start = System.nanoTime();
        var e = assertThrows(ExternalServiceUnavailableException.class,
                () -> summoners.findByPuuid("puuid-tester", Platform.BR1));
        long millis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(e.getMessage().contains("total deadline"), e.getMessage());
        assertTrue(millis < 850, "gave up after " + millis + " ms");
        // O WireMock so grava a requisicao ao fim do atraso (2 s); depois disso nenhuma nova pode chegar.
        awaitRequests(2, path);
        LockSupport.parkNanos(Duration.ofSeconds(1).toNanos());
        assertEquals(2, requests(path), "no request may start after the deadline");
        // Estourar o prazo nao e falha extra: o circuito viu so as 2 tentativas reais.
        assertEquals(2, riotResilience.circuitBreakers().getAllCircuitBreakers().stream()
                .mapToInt(c -> c.getMetrics().getNumberOfFailedCalls()).sum());
    }

    @Test
    void invalidTotalDeadlineIsRejectedAtStartup() {
        assertThrows(IllegalArgumentException.class,
                () -> new RiotResilience(Duration.ofMillis(10), Duration.ZERO, 4, 4, Duration.ZERO));
    }

    private int requests(String path) {
        return wireMock.countRequestsMatching(getRequestedFor(urlPathEqualTo(path)).build()).getCount();
    }

    private void awaitRequests(int count, String path) {
        long limit = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (requests(path) < count && System.nanoTime() < limit) {
            LockSupport.parkNanos(Duration.ofMillis(50).toNanos());
        }
    }
}
