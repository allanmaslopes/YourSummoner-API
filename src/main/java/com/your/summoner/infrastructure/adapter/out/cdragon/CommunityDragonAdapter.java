package com.your.summoner.infrastructure.adapter.out.cdragon;

import com.your.summoner.application.port.out.ChampionStatisticsProvider;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.infrastructure.adapter.out.ExternalCalls;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Retry(name = "cdragon")
@CircuitBreaker(name = "cdragon")
public class CommunityDragonAdapter implements ChampionStatisticsProvider {

    private static final Pattern EMBEDDED_JSON = Pattern.compile("JSON\\.parse\\('(\\{.*?})'\\)", Pattern.DOTALL);
    private static final TypeReference<Map<String, Map<String, Double>>> LANES = new TypeReference<>() {
    };

    private final CommunityDragonApi api;
    private final JsonMapper objectMapper;

    public CommunityDragonAdapter(CommunityDragonApi api, JsonMapper objectMapper) {
        this.api = api;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<Long, Double> pickRates(Lane lane) {
        Map<String, Map<String, Double>> all = parse(ExternalCalls.call(
                CommunityDragonApi.SERVICE, api::championStatisticsScript));
        Map<String, Double> byChampion = all.getOrDefault(lane.name(), Map.of());
        try {
            return byChampion.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(entry -> Long.parseLong(entry.getKey()), Map.Entry::getValue));
        } catch (RuntimeException exception) {
            throw new ExternalBadResponseException(CommunityDragonApi.SERVICE,
                    "Invalid champion statistics", exception);
        }
    }

    /** Extrai {@code {"TOP":{"266":0.01,...},...}} de dentro do bundle JavaScript. */
    Map<String, Map<String, Double>> parse(String script) {
        Matcher matcher = script == null ? null : EMBEDDED_JSON.matcher(script);
        if (matcher == null || !matcher.find()) {
            throw new ExternalBadResponseException(CommunityDragonApi.SERVICE,
                    "Champion statistics not found in the Community Dragon script");
        }
        try {
            Map<String, Map<String, Double>> lanes = objectMapper.readValue(matcher.group(1), LANES);
            if (lanes == null) {
                throw new IllegalArgumentException("null statistics");
            }
            return lanes;
        } catch (Exception exception) {
            throw new ExternalBadResponseException(CommunityDragonApi.SERVICE,
                    "Could not parse champion statistics: " + exception.getMessage(), exception);
        }
    }
}
