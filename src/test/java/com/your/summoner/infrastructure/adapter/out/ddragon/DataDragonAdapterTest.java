package com.your.summoner.infrastructure.adapter.out.ddragon;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThan;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.cache.type=none",
        "yoursummoner.patch-sync.enabled=false",
        "resilience4j.retry.instances.ddragon.max-attempts=3",
        "resilience4j.retry.instances.ddragon.wait-duration=1ms",
        "spring.cloud.openfeign.client.config.ddragon.connect-timeout=2000",
        "spring.cloud.openfeign.client.config.ddragon.read-timeout=8000"
})
class DataDragonAdapterTest {

    private static final WireMockServer WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());
    private static final String PATCH = "16.21.1";
    private static final String LOCALE = "pt_BR";
    private static final String VERSIONS_PATH = "/api/versions.json";
    private static final String CHAMPIONS_PATH = "/cdn/16.21.1/data/pt_BR/champion.json";
    private static final String AHRI_PATH = "/cdn/16.21.1/data/pt_BR/champion/Ahri.json";

    static {
        WIRE_MOCK.start();
    }

    @DynamicPropertySource
    static void dataDragonUrl(DynamicPropertyRegistry registry) {
        registry.add("ddragon.url", WIRE_MOCK::baseUrl);
    }

    @Autowired
    GameDataProvider gameData;

    @BeforeEach
    void resetWireMock() {
        WIRE_MOCK.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @Test
    void versionsReturnsTheCompleteListInUpstreamOrder() {
        stubJson(VERSIONS_PATH, "[\"16.21.1\",\"16.20.1\",\"16.19.1\"]");

        assertEquals(List.of("16.21.1", "16.20.1", "16.19.1"), gameData.versions());
    }

    @Test
    void versionsRejectsAnEmptyListAsBadResponse() {
        stubJson(VERSIONS_PATH, "[]");

        assertThrows(ExternalBadResponseException.class, gameData::versions);
    }

    @Test
    void championsMapsEntriesByTheirNumericKey() {
        stubJson(CHAMPIONS_PATH, championListJson());

        var champions = gameData.champions(PATCH, LOCALE);

        assertEquals("Ahri", champions.get(103L).id());
        assertEquals("Garen", champions.get(86L).id());
        assertEquals(List.of("Mage", "Assassin"), champions.get(103L).tags());
    }

    @Test
    void championDetailRemovesChromaSkins() {
        stubJson(AHRI_PATH, ahriDetailJson());

        var detail = gameData.championDetail(PATCH, LOCALE, "Ahri").orElseThrow();

        assertEquals(List.of("Original", "Dinastia"), detail.skins().stream().map(skin -> skin.name()).toList());
        assertFalse(detail.skins().stream().anyMatch(skin -> skin.id().equals("103001")));
    }

    @Test
    void versions403BecomesUnavailableAndIsRetried() {
        WIRE_MOCK.stubFor(get(urlEqualTo(VERSIONS_PATH)).willReturn(aResponse().withStatus(403)));

        assertThrows(ExternalServiceUnavailableException.class, gameData::versions);
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(VERSIONS_PATH)));
    }

    @Test
    void versions404BecomesUnavailableAndIsRetried() {
        WIRE_MOCK.stubFor(get(urlEqualTo(VERSIONS_PATH)).willReturn(aResponse().withStatus(404)));

        assertThrows(ExternalServiceUnavailableException.class, gameData::versions);
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(VERSIONS_PATH)));
    }

    @Test
    void champions403BecomesUnavailableAndIsRetried() {
        WIRE_MOCK.stubFor(get(urlEqualTo(CHAMPIONS_PATH)).willReturn(aResponse().withStatus(403)));

        assertThrows(ExternalServiceUnavailableException.class, () -> gameData.champions(PATCH, LOCALE));
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(CHAMPIONS_PATH)));
    }

    @Test
    void champions404BecomesUnavailableAndIsRetried() {
        WIRE_MOCK.stubFor(get(urlEqualTo(CHAMPIONS_PATH)).willReturn(aResponse().withStatus(404)));

        assertThrows(ExternalServiceUnavailableException.class, () -> gameData.champions(PATCH, LOCALE));
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(CHAMPIONS_PATH)));
    }

    @Test
    void championDetail404ReturnsEmptyWithoutRetry() {
        WIRE_MOCK.stubFor(get(urlEqualTo(AHRI_PATH)).willReturn(aResponse().withStatus(404)));

        assertTrue(gameData.championDetail(PATCH, LOCALE, "Ahri").isEmpty());
        WIRE_MOCK.verify(exactly(1), getRequestedFor(urlEqualTo(AHRI_PATH)));
    }

    @Test
    void transientServerErrorIsRetriedAndCanRecover() {
        WIRE_MOCK.stubFor(get(urlEqualTo(CHAMPIONS_PATH))
                .inScenario("champion catalog recovers")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("recovered"));
        WIRE_MOCK.stubFor(get(urlEqualTo(CHAMPIONS_PATH))
                .inScenario("champion catalog recovers")
                .whenScenarioStateIs("recovered")
                .willReturn(jsonResponse(championListJson())));

        assertEquals("Ahri", gameData.champions(PATCH, LOCALE).get(103L).id());
        WIRE_MOCK.verify(moreThan(1), getRequestedFor(urlEqualTo(CHAMPIONS_PATH)));
    }

    @Test
    void persistentServerErrorBecomesUnavailable() {
        WIRE_MOCK.stubFor(get(urlEqualTo(AHRI_PATH)).willReturn(aResponse().withStatus(500)));

        assertThrows(ExternalServiceUnavailableException.class,
                () -> gameData.championDetail(PATCH, LOCALE, "Ahri"));
    }

    @Test
    void skinUrlsFollowDataDragonPattern() {
        assertEquals("https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Ahri_1.jpg",
                gameData.skinSplashUrl("Ahri", 1));
        assertEquals("https://ddragon.leagueoflegends.com/cdn/img/champion/loading/Ahri_1.jpg",
                gameData.skinLoadingUrl("Ahri", 1));
    }

    @Test
    void profileAndChampionIconUrlsIncludeTheRequestedPatch() {
        assertEquals("https://ddragon.leagueoflegends.com/cdn/16.21.1/img/profileicon/29.png",
                gameData.profileIconUrl(PATCH, 29));
        assertEquals("https://ddragon.leagueoflegends.com/cdn/16.21.1/img/champion/Ahri.png",
                gameData.championIconUrl(PATCH, "Ahri"));
    }

    private static void stubJson(String path, String body) {
        WIRE_MOCK.stubFor(get(urlEqualTo(path)).willReturn(jsonResponse(body)));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
    }

    private static String championListJson() {
        return """
                {
                  "version": "16.21.1",
                  "data": {
                    "Ahri": {"id":"Ahri","key":"103","name":"Ahri","title":"a Raposa de Nove Caudas","tags":["Mage","Assassin"]},
                    "Garen": {"id":"Garen","key":"86","name":"Garen","title":"O Poder de Demacia","tags":["Fighter","Tank"]}
                  }
                }
                """;
    }

    private static String ahriDetailJson() {
        return """
                {
                  "data": {
                    "Ahri": {
                      "id":"Ahri", "key":"103", "name":"Ahri", "title":"a Raposa de Nove Caudas",
                      "lore":"Lore", "blurb":"Blurb", "tags":["Mage","Assassin"], "partype":"Mana",
                      "info":{"attack":3,"defense":4,"magic":8,"difficulty":5},
                      "passive":{"name":"Furto de Essência","description":"Descrição"},
                      "spells":[],
                      "skins":[
                        {"id":"103000","num":0,"name":"Original","parentSkin":null},
                        {"id":"103001","num":1,"name":"Croma vermelho","parentSkin":103000},
                        {"id":"103002","num":2,"name":"Dinastia","parentSkin":null}
                      ],
                      "allytips":[], "enemytips":[]
                    }
                  }
                }
                """;
    }
}
