package com.your.summoner.infrastructure.adapter.out.ddragon;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prazo total de 400 ms, leitura de 300 ms: a 1a tentativa termina em ~300 ms, a 2a em ~600 ms e a 3a
 * (que terminaria em ~900 ms) nao pode comecar, pois o prazo ja estourou.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.cache.type=none",
        "yoursummoner.patch-sync.enabled=false",
        "resilience4j.retry.instances.ddragon.max-attempts=3",
        "resilience4j.retry.instances.ddragon.wait-duration=1ms",
        "spring.cloud.openfeign.client.config.ddragon.read-timeout=300",
        "ddragon.resilience.total-deadline=400ms"
})
class DataDragonTotalDeadlineTest {

    private static final WireMockServer WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());
    private static final String PATH = "/api/versions.json";

    static {
        WIRE_MOCK.start();
    }

    @DynamicPropertySource
    static void dataDragonUrl(DynamicPropertyRegistry registry) {
        registry.add("ddragon.url", WIRE_MOCK::baseUrl);
    }

    @Autowired
    GameDataProvider gameData;

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @Test
    void dataDragonCallGivesUpAtTotalDeadline() {
        WIRE_MOCK.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withFixedDelay(2000)));

        long start = System.nanoTime();
        var e = assertThrows(ExternalServiceUnavailableException.class, gameData::versions);
        long millis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(e.getMessage().contains("total deadline"), e.getMessage());
        assertTrue(millis < 850, "gave up after " + millis + " ms");
        // O WireMock so grava a requisicao ao fim do atraso (2 s); depois disso nenhuma nova pode chegar.
        awaitRequests(2);
        LockSupport.parkNanos(Duration.ofSeconds(1).toNanos());
        assertEquals(2, requests(), "no request may start after the deadline");
    }

    private int requests() {
        return WIRE_MOCK.countRequestsMatching(getRequestedFor(urlEqualTo(PATH)).build()).getCount();
    }

    private void awaitRequests(int count) {
        long limit = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (requests() < count && System.nanoTime() < limit) {
            LockSupport.parkNanos(Duration.ofMillis(50).toNanos());
        }
    }
}
