package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.infrastructure.adapter.out.CachedCalls;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import com.your.summoner.infrastructure.adapter.out.riot.api.RiotAccountApi;
import com.your.summoner.infrastructure.config.RiotApiProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class RiotAccountAdapter implements AccountProvider {
    private final RiotAccountApi api;
    private final RiotApiProperties properties;
    private final RiotMapper mapper;
    private final RiotResilience resilience;
    private final Cache cache;

    public RiotAccountAdapter(RiotAccountApi api, RiotApiProperties properties, RiotMapper mapper,
                              RiotResilience resilience, CacheManager caches) {
        this.api = api;
        this.properties = properties;
        this.mapper = mapper;
        this.resilience = resilience;
        this.cache = java.util.Objects.requireNonNull(caches.getCache("riot-account"));
    }

    @Override
    public Optional<Account> findByRiotId(RiotId riotId, Region region) {
        try {
            return Optional.of(CachedCalls.get(cache,
                    List.of(riotId.gameName().toLowerCase(Locale.ROOT), riotId.tagLine().toLowerCase(Locale.ROOT), region),
                    () -> {
                        var dto = resilience.call("riot-account", () -> api.byRiotId(
                                URI.create(properties.baseUrlTemplate().replace("{host}", region.host())),
                                riotId.gameName(), riotId.tagLine()));
                        return mapper.toDomain(dto);
                    }));
        } catch (UpstreamNotFoundException e) {
            return Optional.empty();
        }
    }
}
