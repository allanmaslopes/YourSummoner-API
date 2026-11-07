package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.LeagueProvider;
import com.your.summoner.domain.model.LeagueEntry;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.infrastructure.adapter.out.CachedCalls;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import com.your.summoner.infrastructure.adapter.out.riot.api.RiotLeagueApi;
import com.your.summoner.infrastructure.config.RiotApiProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class RiotLeagueAdapter implements LeagueProvider {
    private final RiotLeagueApi api;
    private final RiotApiProperties properties;
    private final RiotMapper mapper;
    private final RiotResilience resilience;
    private final Cache cache;

    public RiotLeagueAdapter(RiotLeagueApi api, RiotApiProperties properties, RiotMapper mapper,
                              RiotResilience resilience, CacheManager caches) {
        this.api = api;
        this.properties = properties;
        this.mapper = mapper;
        this.resilience = resilience;
        this.cache = java.util.Objects.requireNonNull(caches.getCache("riot-league"));
    }

    @Override
    public List<LeagueEntry> findByPuuid(String puuid, Platform platform) {
        try {
            return CachedCalls.get(cache, List.of(puuid, platform), () -> {
                var dto = resilience.call("riot-league", () -> api.byPuuid(
                        URI.create(properties.baseUrlTemplate().replace("{host}", platform.host())), puuid));
                return dto == null ? List.of() : dto.stream().map(mapper::toDomain).toList();
            });
        } catch (UpstreamNotFoundException e) {
            return List.of();
        }
    }
}
