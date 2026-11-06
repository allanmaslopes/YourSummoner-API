package com.your.summoner.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.your.summoner.YourSummonerApplication;
import com.your.summoner.infrastructure.adapter.out.riot.RiotResilience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/** Shared isolated Riot server; fixtures stay here because Task 3 owns no resource files. */
@SpringBootTest(classes = YourSummonerApplication.class, properties = {
        "riot.api.token=test-key", "riot.api.read-timeout=300ms",
        "riot.resilience.retry-delay=10ms", "riot.resilience.retry-jitter=0ms",
        "riot.resilience.minimum-calls=4", "riot.resilience.sliding-window=4",
        "yoursummoner.patch-sync.enabled=false",
        // These tests never touch Mongo; skip the embedded server.
        "spring.autoconfigure.exclude=de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration"})
public abstract class RiotApiWireMock {
    public static final String RIOT = "/riot";
    protected static final WireMockServer wireMock = new WireMockServer(options().dynamicPort());

    static {
        wireMock.start();
    }

    @DynamicPropertySource
    static void riotProperties(DynamicPropertyRegistry registry) {
        registry.add("riot.api.base-url-template", () -> wireMock.baseUrl() + RIOT + "/{host}");
    }

    @Autowired CacheManager caches;
    @Autowired RiotResilience resilience;

    @BeforeEach
    @AfterEach
    protected void resetRiotState() {
        caches.getCacheNames().forEach(name -> caches.getCache(name).clear());
        resilience.circuitBreakers().getAllCircuitBreakers().forEach(io.github.resilience4j.circuitbreaker.CircuitBreaker::reset);
        resilience.retries().getAllRetries().forEach(retry -> resilience.retries().remove(retry.getName()));
        wireMock.resetAll();
        stubDefaults(wireMock);
    }

    public static void stubDefaults(WireMockServer server) {
        json(server, RIOT + "/americas/riot/account/v1/accounts/by-riot-id/Tester/BR1", "account-tester.json");
        json(server, RIOT + "/br1/lol/summoner/v4/summoners/by-puuid/puuid-tester", "summoner-tester.json");
        json(server, RIOT + "/br1/lol/league/v4/entries/by-puuid/puuid-tester", "league-tester.json");
        json(server, RIOT + "/br1/lol/champion-mastery/v4/champion-masteries/by-puuid/puuid-tester", "mastery-tester.json");
        json(server, RIOT + "/americas/lol/match/v5/matches/by-puuid/puuid-tester/ids", "match-ids-tester.json");
        json(server, RIOT + "/americas/lol/match/v5/matches/BR1_3000000001", "match-BR1_3000000001.json");
    }

    public static void json(WireMockServer server, String path, String file) {
        server.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody(body(file))));
    }

    public static String riotError(int status, String message) {
        return "{\"status\":{\"message\":\"%s\",\"status_code\":%d}}".formatted(message, status);
    }

    public static String body(String file) {
        return switch (file) {
            case "account-tester.json" -> """
                    {"puuid":"puuid-tester","gameName":"Tester","tagLine":"BR1"}
                    """;
            case "summoner-tester.json" -> """
                    {"puuid":"puuid-tester","profileIconId":4568,"revisionDate":1792169607000,"summonerLevel":150}
                    """;
            case "league-tester.json" -> """
                    [{"leagueId":"x","queueType":"RANKED_SOLO_5x5","tier":"GOLD","rank":"II","puuid":"puuid-tester","leaguePoints":42,"wins":55,"losses":45,"veteran":false,"inactive":false,"freshBlood":false,"hotStreak":true},{"queueType":"RANKED_FLEX_SR","tier":"SILVER","rank":"I","puuid":"puuid-tester","leaguePoints":10,"wins":3,"losses":7,"hotStreak":false}]
                    """;
            case "mastery-tester.json" -> """
                    [{"puuid":"puuid-tester","championId":64,"championLevel":30,"championPoints":250000,"lastPlayTime":1792168334000,"markRequiredForNextLevel":2,"tokensEarned":1},{"puuid":"puuid-tester","championId":103,"championLevel":20,"championPoints":180000,"lastPlayTime":1790640239000},{"puuid":"puuid-tester","championId":9999,"championLevel":10,"championPoints":150000,"lastPlayTime":1790640239000},{"puuid":"puuid-tester","championId":266,"championLevel":12,"championPoints":90000,"lastPlayTime":1790640239000},{"puuid":"puuid-tester","championId":222,"championLevel":5,"championPoints":20000,"lastPlayTime":1790640239000}]
                    """;
            case "match-ids-tester.json" -> """
                    ["BR1_3000000002","BR1_3000000001"]
                    """;
            case "match-BR1_3000000001.json" -> """
                    {"metadata":{"dataVersion":"2","matchId":"BR1_3000000001","participants":["puuid-tester","puuid-other"]},"info":{"gameCreation":1792160000000,"gameDuration":1834,"gameEndTimestamp":1792161834000,"gameMode":"CLASSIC","gameType":"MATCHED_GAME","gameVersion":"16.21.712.1234","mapId":11,"platformId":"BR1","queueId":420,"participants":[{"puuid":"puuid-tester","riotIdGameName":"Tester","riotIdTagline":"BR1","teamId":100,"championId":64,"championName":"LeeSin","teamPosition":"JUNGLE","champLevel":16,"kills":8,"deaths":3,"assists":10,"totalMinionsKilled":40,"neutralMinionsKilled":160,"goldEarned":13500,"totalDamageDealtToChampions":21000,"visionScore":35,"win":true,"item0":6692,"item1":3071,"item2":3047,"item3":0,"item4":0,"item5":0,"item6":3364,"summoner1Id":4,"summoner2Id":11},{"puuid":"puuid-other","riotIdGameName":"Other","riotIdTagline":"BR1","teamId":200,"championId":103,"championName":"Ahri","teamPosition":"MIDDLE","champLevel":15,"kills":2,"deaths":0,"assists":1,"win":false,"summoner1Id":4,"summoner2Id":14}],"teams":[{"teamId":100,"win":true,"bans":[{"championId":222,"pickTurn":1}],"objectives":{"baron":{"first":true,"kills":1},"dragon":{"first":true,"kills":3},"tower":{"first":true,"kills":9},"champion":{"first":true,"kills":30}}},{"teamId":200,"win":false,"bans":[{"championId":-1,"pickTurn":6}],"objectives":{"baron":{"first":false,"kills":0},"dragon":{"first":false,"kills":1},"tower":{"first":false,"kills":2},"champion":{"first":false,"kills":12}}}]}}
                    """;
            default -> throw new IllegalArgumentException("Unknown Riot fixture: " + file);
        };
    }
}
