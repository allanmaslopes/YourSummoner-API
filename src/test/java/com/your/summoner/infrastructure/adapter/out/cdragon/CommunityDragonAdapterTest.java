package com.your.summoner.infrastructure.adapter.out.cdragon;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.your.summoner.application.port.out.ChampionStatisticsProvider;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.model.Lane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThan;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.cache.type=none",
        "yoursummoner.patch-sync.enabled=false",
        "resilience4j.retry.instances.cdragon.max-attempts=3",
        "resilience4j.retry.instances.cdragon.wait-duration=1ms",
        "spring.cloud.openfeign.client.config.cdragon.connect-timeout=2000",
        "spring.cloud.openfeign.client.config.cdragon.read-timeout=8000"
})
class CommunityDragonAdapterTest {

    private static final WireMockServer WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());
    private static final String SCRIPT_PATH =
            "/latest/plugins/rcp-fe-lol-champion-statistics/global/default/rcp-fe-lol-champion-statistics.js";

    static {
        WIRE_MOCK.start();
    }

    @DynamicPropertySource
    static void communityDragonUrl(DynamicPropertyRegistry registry) {
        registry.add("cdragon.url", WIRE_MOCK::baseUrl);
    }

    @Autowired
    CommunityDragonAdapter adapter;

    @Autowired
    ChampionStatisticsProvider statistics;

    @BeforeEach
    void resetWireMock() {
        WIRE_MOCK.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @Test
    void extractsTheJsonEmbeddedInTheScript() {
        String script = "(()=>{var e=[,e=>{e.exports=JSON.parse('{\"TOP\":{\"266\":0.05},"
                + "\"JUNGLE\":{\"64\":0.06381}}')},(e,t,n)=>{n.r(t)}];})();";

        var lanes = adapter.parse(script);

        assertEquals(0.05, lanes.get("TOP").get("266"));
        assertEquals(0.06381, lanes.get("JUNGLE").get("64"));
    }

    @Test
    void missingEmbeddedJsonIsBadResponse() {
        var exception = assertThrows(ExternalBadResponseException.class,
                () -> adapter.parse("console.log('nothing here')"));

        assertEquals(ExternalBadResponseException.Reason.BAD_RESPONSE, exception.reason());
    }

    @Test
    void invalidEmbeddedJsonIsBadResponse() {
        var exception = assertThrows(ExternalBadResponseException.class,
                () -> adapter.parse("JSON.parse('{not-json}')"));

        assertEquals(ExternalBadResponseException.Reason.BAD_RESPONSE, exception.reason());
    }

    @Test
    void missingLaneReturnsAnEmptyMap() {
        stubScript(scriptWithStatistics());

        assertEquals(0, statistics.pickRates(Lane.SUPPORT).size());
    }

    @Test
    void pickRatesUseLongIdsAndReturnAnImmutableMap() {
        stubScript(scriptWithStatistics());

        var pickRates = statistics.pickRates(Lane.TOP);

        assertEquals(0.05, pickRates.get(266L));
        assertEquals(0.023, pickRates.get(103L));
        assertThrows(UnsupportedOperationException.class, () -> pickRates.put(86L, 0.04));
    }

    @Test
    void transientServerErrorIsRetriedAndCanRecover() {
        WIRE_MOCK.stubFor(get(urlEqualTo(SCRIPT_PATH))
                .inScenario("statistics recover")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(502))
                .willSetStateTo("recovered"));
        WIRE_MOCK.stubFor(get(urlEqualTo(SCRIPT_PATH))
                .inScenario("statistics recover")
                .whenScenarioStateIs("recovered")
                .willReturn(scriptResponse(scriptWithStatistics())));

        assertEquals(0.05, statistics.pickRates(Lane.TOP).get(266L));
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(SCRIPT_PATH)));
    }

    @Test
    void persistentServerErrorBecomesUnavailable() {
        WIRE_MOCK.stubFor(get(urlEqualTo(SCRIPT_PATH)).willReturn(aResponse().withStatus(503)));

        assertThrows(ExternalServiceUnavailableException.class, () -> statistics.pickRates(Lane.TOP));
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(SCRIPT_PATH)));
    }

    @Test
    void clientErrorIsBadResponseAndIsNotRetried() {
        WIRE_MOCK.stubFor(get(urlEqualTo(SCRIPT_PATH)).willReturn(aResponse().withStatus(404)));

        assertThrows(ExternalBadResponseException.class, () -> statistics.pickRates(Lane.TOP));
        WIRE_MOCK.verify(exactly(1), getRequestedFor(urlEqualTo(SCRIPT_PATH)));
    }

    private static void stubScript(String script) {
        WIRE_MOCK.stubFor(get(urlEqualTo(SCRIPT_PATH)).willReturn(scriptResponse(script)));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder scriptResponse(String script) {
        return aResponse().withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withBody(script);
    }

    private static String scriptWithStatistics() {
        return "(()=>{var e=[,e=>{e.exports=JSON.parse('{\"TOP\":{\"266\":0.05,\"103\":0.023},"
                + "\"JUNGLE\":{\"64\":0.06381}}')}];})();";
    }
}
