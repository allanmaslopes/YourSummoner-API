package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.model.SummonerDetails;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.infrastructure.adapter.out.CachedCalls;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import com.your.summoner.infrastructure.adapter.out.riot.api.RiotSummonerApi;
import com.your.summoner.infrastructure.config.RiotApiProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class RiotSummonerAdapter implements SummonerProvider {
    private final RiotSummonerApi api;
    private final RiotApiProperties properties;
    private final RiotMapper mapper;
    private final RiotResilience resilience;
    private final Cache cache;

    public RiotSummonerAdapter(RiotSummonerApi api, RiotApiProperties properties, RiotMapper mapper,
                              RiotResilience resilience, CacheManager caches) {
        this.api = api;
        this.properties = properties;
        this.mapper = mapper;
        this.resilience = resilience;
        this.cache = java.util.Objects.requireNonNull(caches.getCache("riot-summoner"));
    }

    @Override
    public Optional<SummonerDetails> findByPuuid(String puuid, Platform platform) {
        try {
            return Optional.of(CachedCalls.get(cache, List.of(puuid, platform), () -> {
                var dto = resilience.call("riot-summoner", () -> api.byPuuid(
                        URI.create(properties.baseUrlTemplate().replace("{host}", platform.host())), puuid));
                return mapper.toDomain(dto);
            }));
        } catch (UpstreamNotFoundException e) {
            return Optional.empty();
        }
    }
}
