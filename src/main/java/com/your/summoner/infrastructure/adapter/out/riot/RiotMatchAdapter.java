package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.MatchProvider;
import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Region;
import com.your.summoner.infrastructure.adapter.out.CachedCalls;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import com.your.summoner.infrastructure.adapter.out.riot.api.RiotMatchApi;
import com.your.summoner.infrastructure.config.RiotApiProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class RiotMatchAdapter implements MatchProvider {
    private final RiotMatchApi api;
    private final RiotApiProperties properties;
    private final RiotMapper mapper;
    private final RiotResilience resilience;
    private final Cache idsCache;
    private final Cache matchCache;

    public RiotMatchAdapter(RiotMatchApi api, RiotApiProperties properties, RiotMapper mapper,
                            RiotResilience resilience, CacheManager caches) {
        this.api = api;
        this.properties = properties;
        this.mapper = mapper;
        this.resilience = resilience;
        this.idsCache = java.util.Objects.requireNonNull(caches.getCache("riot-match-ids"));
        this.matchCache = java.util.Objects.requireNonNull(caches.getCache("riot-match"));
    }

    @Override
    public List<String> findMatchIds(String puuid, Region region, MatchQuery query) {
        try {
            return CachedCalls.get(idsCache, List.of(puuid, region, query), () -> {
                var ids = resilience.call("riot-match", () -> api.idsByPuuid(baseUri(region), puuid,
                        query.startTime(), query.endTime(), query.queue(), query.type(), query.start(), query.count()));
                return ids == null ? List.of() : List.copyOf(ids);
            });
        } catch (UpstreamNotFoundException e) {
            return List.of();
        }
    }

    @Override
    public Optional<Match> findById(String matchId, Region region) {
        try {
            return Optional.of(CachedCalls.get(matchCache, List.of(matchId, region), () ->
                    mapper.toDomain(resilience.call("riot-match", () -> api.byId(baseUri(region), matchId)))));
        } catch (UpstreamNotFoundException e) {
            return Optional.empty();
        }
    }

    private URI baseUri(Region region) {
        return URI.create(properties.baseUrlTemplate().replace("{host}", region.host()));
    }
}
