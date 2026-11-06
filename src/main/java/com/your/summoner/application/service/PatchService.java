package com.your.summoner.application.service;

import com.your.summoner.application.port.in.ListPatchesUseCase;
import com.your.summoner.application.port.in.SyncPatchesUseCase;
import com.your.summoner.application.port.out.ChampionStatisticsProvider;
import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.application.port.out.StaticDataRepository;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Service
public class PatchService implements SyncPatchesUseCase, ListPatchesUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(PatchService.class);
    private static final int MAX_CONCURRENT_DETAILS = 8;

    private final GameDataProvider gameData;
    private final ChampionStatisticsProvider statistics;
    private final StaticDataRepository repository;
    private final CatalogSettings settings;

    public PatchService(GameDataProvider gameData, ChampionStatisticsProvider statistics,
                        StaticDataRepository repository, CatalogSettings settings) {
        this.gameData = gameData;
        this.statistics = statistics;
        this.repository = repository;
        this.settings = settings;
    }

    @Override
    public List<Patch> execute() {
        return repository.findAllPatches();
    }

    @Override
    public void syncLatest() {
        String locale = settings.locale();
        List<String> versions = gameData.versions();
        repository.mergeVersions(versions);
        String latest = versions.getFirst();
        Optional<Patch> activeBefore = repository.findActivePatch();
        Patch latestPatch = repository.findPatch(latest).orElseThrow(() ->
                new IllegalStateException("Patch " + latest + " missing after mergeVersions"));

        // Só o patch mais novo recebe pick rate: o Community Dragon serve apenas o "latest".
        boolean latestReady = latestPatch.isReady(locale) || ingestQuietly(latest, true);
        if (latestReady && !latestPatch.active()) {
            repository.activate(latest);
        }
        // Troca de locale: o patch ativo precisa existir no idioma novo, sem trocar o ativo.
        if (!latestReady) {
            activeBefore.filter(active -> !active.isReady(locale))
                    .ifPresent(active -> ingestQuietly(active.version(), false));
        }
    }

    @Override
    public void ingest(String version) {
        ingest(version, false, true);
    }

    private boolean ingestQuietly(String version, boolean withPickRates) {
        return ingest(version, withPickRates, false);
    }

    private boolean ingest(String version, boolean withPickRates, boolean rethrow) {
        String locale = settings.locale();
        if (!repository.tryLock(version, locale, Instant.now(), settings.lockStaleAfter())) {
            LOG.info("Patch {} ({}) is already being ingested, skipping", version, locale);
            return false;
        }
        try {
            Map<Long, Champion> champions = gameData.champions(version, locale);
            repository.saveChampions(version, locale, champions.values());
            saveDetailsAndSkins(version, locale, champions.values());
            if (withPickRates) {
                savePickRates(version);
            }
            repository.markReady(version, locale);
            LOG.info("Patch {} ({}) ingested: {} champions", version, locale, champions.size());
            return true;
        } catch (RuntimeException e) {
            try {
                repository.markFailed(version, locale);
            } catch (RuntimeException markFailure) {
                e.addSuppressed(markFailure);
            }
            if (rethrow) {
                throw e;
            }
            LOG.warn("Ingestion of patch {} ({}) failed, previous active patch is kept: {}", version, locale, e.toString());
            return false;
        }
    }

    private void saveDetailsAndSkins(String version, String locale, Collection<Champion> champions) {
        Semaphore permits = new Semaphore(MAX_CONCURRENT_DETAILS);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ChampionDetail>> futures = new ArrayList<>();
            for (Champion champion : champions) {
                futures.add(executor.submit(() -> {
                    permits.acquire();
                    try {
                        return gameData.championDetail(version, locale, champion.id()).orElseThrow(() ->
                                new ExternalServiceUnavailableException("ddragon",
                                        "Detail of %s not available for patch %s".formatted(champion.id(), version)));
                    } finally {
                        permits.release();
                    }
                }));
            }
            try {
                for (Future<ChampionDetail> future : futures) {
                    ChampionDetail detail = future.get();
                    repository.saveChampionDetail(version, locale, detail);
                    repository.saveSkins(version, locale, Skins.of(gameData, detail));
                }
            } catch (ExecutionException e) {
                executor.shutdownNow();
                throw e.getCause() instanceof RuntimeException r ? r : new IllegalStateException(e.getCause());
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while ingesting patch " + version, e);
            }
        }
    }

    private void savePickRates(String version) {
        for (Lane lane : Lane.values()) {
            try {
                repository.savePickRates(version, lane, statistics.pickRates(lane));
            } catch (RuntimeException e) {
                // Pick rate não condiciona o READY do patch (seção 6.1 do design).
                LOG.warn("Pick rates for {} not saved: {}", lane, e.toString());
            }
        }
    }
}
