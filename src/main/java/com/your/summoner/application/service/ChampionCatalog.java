package com.your.summoner.application.service;

import com.your.summoner.application.port.in.SyncPatchesUseCase;
import com.your.summoner.application.port.out.CatalogStorageUnavailableException;
import com.your.summoner.application.port.out.ChampionStatisticsProvider;
import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.application.port.out.StaticDataRepository;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Leitura do catálogo: Mongo (com L1 no adapter) → Data Dragon. O Mongo é cache, não fonte da verdade:
 * se cair, a leitura vai direto ao Data Dragon.
 */
@Service
public class ChampionCatalog {

    private static final Logger LOG = LoggerFactory.getLogger(ChampionCatalog.class);

    private final StaticDataRepository repository;
    private final GameDataProvider gameData;
    private final ChampionStatisticsProvider statistics;
    private final SyncPatchesUseCase sync;
    private final CatalogSettings settings;

    public ChampionCatalog(StaticDataRepository repository, GameDataProvider gameData,
                           ChampionStatisticsProvider statistics, SyncPatchesUseCase sync, CatalogSettings settings) {
        this.repository = repository;
        this.gameData = gameData;
        this.statistics = statistics;
        this.sync = sync;
        this.settings = settings;
    }

    /** Patch ativo; durante a ingestão de um novo, continua o anterior. */
    public String activePatch() {
        try {
            Optional<Patch> active = repository.findActivePatch();
            if (active.isPresent()) {
                return active.get().version();
            }
        } catch (CatalogStorageUnavailableException e) {
            LOG.warn("Catalog storage unavailable, reading the active patch from Data Dragon: {}", e.getMessage());
        }
        return gameData.versions().getFirst();
    }

    /** Vazio ou nulo → patch ativo. Versão inválida ou desconhecida → 404, sem ingestão. */
    public String resolve(String requestedPatch) {
        if (requestedPatch == null || requestedPatch.isBlank()) {
            return activePatch();
        }
        if (!Patch.isValidVersion(requestedPatch)) {
            throw new ResourceNotFoundException("Patch", requestedPatch);
        }
        boolean known;
        try {
            known = repository.findPatch(requestedPatch).isPresent();
        } catch (CatalogStorageUnavailableException e) {
            LOG.warn("Catalog storage unavailable, checking patch {} against Data Dragon: {}",
                    requestedPatch, e.getMessage());
            known = gameData.versions().contains(requestedPatch);
        }
        if (!known) {
            throw new ResourceNotFoundException("Patch", requestedPatch);
        }
        return requestedPatch;
    }

    public Map<Long, Champion> champions(String patch) {
        String locale = settings.locale();
        return read(patch, locale, () -> repository.findChampions(patch, locale), Map::isEmpty,
                () -> gameData.champions(patch, locale));
    }

    public Optional<ChampionDetail> championDetail(String patch, String championId) {
        String locale = settings.locale();
        return read(patch, locale, () -> repository.findChampionDetail(patch, locale, championId), Optional::isEmpty,
                () -> gameData.championDetail(patch, locale, championId));
    }

    public List<ChampionSkin> skins(String patch, String championId) {
        String locale = settings.locale();
        return read(patch, locale, () -> repository.findSkins(patch, locale, championId), List::isEmpty,
                () -> gameData.championDetail(patch, locale, championId)
                        .map(detail -> Skins.of(gameData, detail)).orElse(List.of()));
    }

    /** Pick rate do patch ativo (o Community Dragon só serve o "latest"). */
    public Map<Long, Double> pickRates(Lane lane) {
        try {
            Optional<Patch> active = repository.findActivePatch();
            if (active.isPresent()) {
                Optional<Map<Long, Double>> stored = repository.findPickRates(active.get().version(), lane);
                if (stored.isPresent()) {
                    return stored.get();
                }
            }
        } catch (CatalogStorageUnavailableException e) {
            LOG.warn("Catalog storage unavailable, reading pick rates directly: {}", e.getMessage());
        }
        return statistics.pickRates(lane);
    }

    public String profileIconUrl(String patch, int id) {
        return gameData.profileIconUrl(patch, id);
    }

    public String championIconUrl(String patch, String championId) {
        return gameData.championIconUrl(patch, championId);
    }

    /**
     * Lê do Mongo. Vazio e patch já {@code READY} → vazio é a resposta (ex.: campeão inexistente).
     * Vazio e patch conhecido mas não pronto → ingere sob demanda e lê de novo. Mongo fora, patch
     * desconhecido ou ingestão travada por outra instância → leitura direta do Data Dragon.
     */
    private <T> T read(String patch, String locale, Supplier<T> fromStore, Predicate<T> isEmpty, Supplier<T> direct) {
        try {
            T value = fromStore.get();
            if (!isEmpty.test(value)) {
                return value;
            }
            Optional<Patch> known = repository.findPatch(patch);
            if (known.isPresent()) {
                if (known.get().isReady(locale)) {
                    return value;
                }
                sync.ingest(patch);
                value = fromStore.get();
                if (!isEmpty.test(value)) {
                    return value;
                }
            }
        } catch (CatalogStorageUnavailableException e) {
            LOG.warn("Catalog storage unavailable, reading patch {} directly from Data Dragon: {}",
                    patch, e.getMessage());
        }
        return direct.get();
    }
}
