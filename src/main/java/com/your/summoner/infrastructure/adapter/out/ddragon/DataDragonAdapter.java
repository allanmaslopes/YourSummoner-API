package com.your.summoner.infrastructure.adapter.out.ddragon;

import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.infrastructure.adapter.out.DeadlineRetry;
import com.your.summoner.infrastructure.adapter.out.ExternalCalls;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import com.your.summoner.infrastructure.adapter.out.ddragon.dto.DdChampionDetailListDto;
import com.your.summoner.infrastructure.adapter.out.ddragon.dto.DdChampionListDto;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DataDragonAdapter implements GameDataProvider {

    private static final String PUBLIC_CDN = "https://ddragon.leagueoflegends.com";

    static final String DEFAULT_TOTAL_DEADLINE = "12s";

    private final DataDragonApi api;
    private final RetryRegistry retries;
    private final CircuitBreakerRegistry circuitBreakers;
    private final Duration totalDeadline;

    public DataDragonAdapter(DataDragonApi api, RetryRegistry retries, CircuitBreakerRegistry circuitBreakers,
                             @Value("${ddragon.resilience.total-deadline:" + DEFAULT_TOTAL_DEADLINE + "}")
                             Duration totalDeadline) {
        this.api = api;
        this.retries = retries;
        this.circuitBreakers = circuitBreakers;
        this.totalDeadline = DeadlineRetry.requirePositive("ddragon.resilience.total-deadline", totalDeadline);
    }

    /** Retry com prazo total por fora, circuit breaker por tentativa (mesma regra da Riot). */
    private <T> T call(String notFoundAsUnavailable, Supplier<T> request) {
        var circuit = circuitBreakers.circuitBreaker("ddragon");
        return DeadlineRetry.call(DataDragonApi.SERVICE, totalDeadline, retries.retry("ddragon"),
                () -> {
                    try {
                        return circuit.executeSupplier(() -> ExternalCalls.call(DataDragonApi.SERVICE, request));
                    } catch (UpstreamNotFoundException exception) {
                        // 403/404 do CDN no dia do patch: retentável, mas não é falha do circuito.
                        if (notFoundAsUnavailable == null) {
                            throw exception;
                        }
                        throw new ExternalServiceUnavailableException(DataDragonApi.SERVICE,
                                notFoundAsUnavailable, exception);
                    }
                });
    }

    @Override
    public List<String> versions() {
        List<String> versions = call("versions.json not available", api::versions);
        if (versions == null || versions.isEmpty() || versions.stream().anyMatch(this::isBlank)) {
            throw badResponse("Empty or invalid versions list");
        }
        return List.copyOf(versions);
    }

    @Override
    public Map<Long, Champion> champions(String patch, String locale) {
        DdChampionListDto dto = call("champion.json not available for version " + patch,
                () -> api.champions(patch, locale));
        if (dto == null || dto.data() == null) {
            throw badResponse("Invalid champion list for version " + patch);
        }
        try {
            return dto.data().values().stream()
                    .map(this::toChampion)
                    .collect(Collectors.toUnmodifiableMap(Champion::key, Function.identity()));
        } catch (RuntimeException exception) {
            if (exception instanceof ExternalBadResponseException badResponse) {
                throw badResponse;
            }
            throw new ExternalBadResponseException(DataDragonApi.SERVICE,
                    "Invalid champion list for version " + patch, exception);
        }
    }

    @Override
    public Optional<ChampionDetail> championDetail(String patch, String locale, String championId) {
        DdChampionDetailListDto dto;
        try {
            dto = call(null, () -> api.championDetail(patch, locale, championId));
        } catch (UpstreamNotFoundException exception) {
            return Optional.empty();
        }
        if (dto == null || dto.data() == null) {
            throw badResponse("Invalid champion detail for " + championId);
        }
        DdChampionDetailListDto.Detail detail = dto.data().get(championId);
        if (detail == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(toChampionDetail(patch, detail));
        } catch (RuntimeException exception) {
            throw new ExternalBadResponseException(DataDragonApi.SERVICE,
                    "Invalid champion detail for " + championId, exception);
        }
    }

    @Override
    public String profileIconUrl(String patch, int profileIconId) {
        return "%s/cdn/%s/img/profileicon/%d.png".formatted(PUBLIC_CDN, patch, profileIconId);
    }

    @Override
    public String championIconUrl(String patch, String championId) {
        return "%s/cdn/%s/img/champion/%s.png".formatted(PUBLIC_CDN, patch, championId);
    }

    @Override
    public String skinSplashUrl(String championId, int num) {
        return "%s/cdn/img/champion/splash/%s_%d.jpg".formatted(PUBLIC_CDN, championId, num);
    }

    @Override
    public String skinLoadingUrl(String championId, int num) {
        return "%s/cdn/img/champion/loading/%s_%d.jpg".formatted(PUBLIC_CDN, championId, num);
    }

    private Champion toChampion(DdChampionListDto.Champion champion) {
        if (champion == null || isBlank(champion.id()) || isBlank(champion.key())) {
            throw badResponse("Invalid champion entry");
        }
        return new Champion(champion.id(), Long.parseLong(champion.key()), champion.name(), champion.title(),
                champion.tags());
    }

    private ChampionDetail toChampionDetail(String patch, DdChampionDetailListDto.Detail detail) {
        if (isBlank(detail.id()) || isBlank(detail.key())) {
            throw badResponse("Invalid champion detail entry");
        }
        ChampionDetail.Info info = detail.info() == null ? null
                : new ChampionDetail.Info(detail.info().attack(), detail.info().defense(), detail.info().magic(),
                detail.info().difficulty());
        ChampionDetail.Passive passive = detail.passive() == null ? null
                : new ChampionDetail.Passive(detail.passive().name(), detail.passive().description());
        List<ChampionDetail.Spell> spells = detail.spells() == null ? List.of() : detail.spells().stream()
                .map(spell -> new ChampionDetail.Spell(spell.id(), spell.name(), spell.description(),
                        spell.cooldownBurn(), spell.costBurn(), spell.rangeBurn()))
                .toList();
        List<ChampionDetail.Skin> skins = detail.skins() == null ? List.of() : detail.skins().stream()
                .filter(skin -> skin.parentSkin() == null)
                .map(skin -> new ChampionDetail.Skin(skin.id(), skin.num(), skin.name()))
                .toList();
        return new ChampionDetail(detail.id(), Long.parseLong(detail.key()), detail.name(), detail.title(),
                detail.lore(), detail.blurb(), detail.tags(), detail.partype(), info, passive, spells, skins,
                detail.allytips(), detail.enemytips(), championIconUrl(patch, detail.id()));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ExternalBadResponseException badResponse(String message) {
        return new ExternalBadResponseException(DataDragonApi.SERVICE, message);
    }
}
