package com.your.summoner.application.service;

import com.your.summoner.application.port.out.ChampionStatisticsProvider;
import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.application.port.out.StaticDataRepository;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import com.your.summoner.domain.model.PatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PatchServiceTest {

    private static final String OLD = "14.1.1";
    private static final String NEW = "14.2.1";
    private static final String PT_BR = "pt_BR";

    @Mock GameDataProvider gameData;
    @Mock ChampionStatisticsProvider statistics;
    @Mock StaticDataRepository repository;

    private PatchService service;

    @BeforeEach
    void setUp() {
        service = new PatchService(gameData, statistics, repository, new CatalogSettings(() -> PT_BR, Duration.ofMinutes(15)));
        when(gameData.versions()).thenReturn(List.of(NEW, OLD));
        when(gameData.skinSplashUrl(anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(i -> "splash/" + i.getArgument(0) + "_" + i.getArgument(1));
        when(gameData.skinLoadingUrl(anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(i -> "loading/" + i.getArgument(0) + "_" + i.getArgument(1));
        when(statistics.pickRates(any())).thenReturn(Map.of(266L, 0.05));
        when(repository.tryLock(anyString(), anyString(), any(), any())).thenReturn(true);
    }

    @Test
    void syncLatestIngestsThenActivates() {
        patches(patch(OLD, true, PT_BR, PatchStatus.READY), patch(NEW, false));
        stubChampions(NEW, "Aatrox", "Ahri");

        service.syncLatest();

        InOrder order = inOrder(repository);
        order.verify(repository).mergeVersions(List.of(NEW, OLD));
        order.verify(repository).tryLock(eq(NEW), eq(PT_BR), any(), eq(Duration.ofMinutes(15)));
        order.verify(repository).saveChampions(eq(NEW), eq(PT_BR), anyCollection());
        order.verify(repository, org.mockito.Mockito.atLeastOnce()).saveChampionDetail(eq(NEW), eq(PT_BR), any());
        order.verify(repository, org.mockito.Mockito.atLeastOnce()).saveSkins(eq(NEW), eq(PT_BR), anyList());
        order.verify(repository, org.mockito.Mockito.times(Lane.values().length))
                .savePickRates(eq(NEW), any(Lane.class), anyMap());
        order.verify(repository).markReady(NEW, PT_BR);
        order.verify(repository).activate(NEW);
        verify(repository, org.mockito.Mockito.times(2)).saveChampionDetail(eq(NEW), eq(PT_BR), any());
        verify(repository, org.mockito.Mockito.times(2)).saveSkins(eq(NEW), eq(PT_BR), anyList());
        verify(repository, never()).markFailed(anyString(), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void ingestionStoresSkinsWithUrlsAndEveryLanePickRate() {
        patches(patch(NEW, false));
        when(gameData.versions()).thenReturn(List.of(NEW));
        stubChampions(NEW, "Ahri");

        service.syncLatest();

        ArgumentCaptor<List<ChampionSkin>> skins = ArgumentCaptor.forClass(List.class);
        verify(repository).saveSkins(eq(NEW), eq(PT_BR), skins.capture());
        assertThat(skins.getValue()).containsExactly(
                new ChampionSkin("Ahri", "103000", 0, "default", "splash/Ahri_0", "loading/Ahri_0"),
                new ChampionSkin("Ahri", "103001", 1, "Dynasty", "splash/Ahri_1", "loading/Ahri_1"));
        for (Lane lane : Lane.values()) {
            verify(repository).savePickRates(NEW, lane, Map.of(266L, 0.05));
        }
    }

    @Test
    void activePatchStaysOldWhileNewIsIngesting() {
        Patch old = patch(OLD, true, PT_BR, PatchStatus.READY);
        patches(old, patch(NEW, false));
        when(repository.findActivePatch()).thenReturn(Optional.of(old));
        stubChampions(NEW, "Ahri");
        ChampionCatalog catalog = new ChampionCatalog(repository, gameData, statistics, service,
                new CatalogSettings(() -> PT_BR, Duration.ofMinutes(15)));
        List<String> seenDuringIngestion = new java.util.ArrayList<>();
        org.mockito.Mockito.doAnswer(i -> {
            seenDuringIngestion.add(catalog.activePatch());
            return null;
        }).when(repository).saveChampions(eq(NEW), eq(PT_BR), anyCollection());

        service.syncLatest();

        assertThat(seenDuringIngestion).containsExactly(OLD);
        verify(repository).activate(NEW);
    }

    @Test
    void failedIngestionKeepsPreviousActive() {
        patches(patch(OLD, true, PT_BR, PatchStatus.READY), patch(NEW, false));
        stubChampions(NEW, "Ahri", "Aatrox");
        when(gameData.championDetail(NEW, PT_BR, "Aatrox")).thenThrow(
                new ExternalServiceUnavailableException("ddragon", "boom"));

        service.syncLatest();

        verify(repository).markFailed(NEW, PT_BR);
        verify(repository, never()).markReady(anyString(), anyString());
        verify(repository, never()).activate(anyString());
    }

    @Test
    void syncLatestIsNoOpWhenLatestAlreadyReady() {
        patches(patch(NEW, true, PT_BR, PatchStatus.READY), patch(OLD, false));

        service.syncLatest();

        verify(repository, never()).tryLock(anyString(), anyString(), any(), any());
        verify(repository, never()).activate(anyString());
        verify(gameData, never()).champions(anyString(), anyString());
    }

    @Test
    void lockedPatchIsSkipped() {
        patches(patch(OLD, true, PT_BR, PatchStatus.READY), patch(NEW, false));
        when(repository.tryLock(eq(NEW), anyString(), any(), any())).thenReturn(false);

        service.syncLatest();

        verify(repository, never()).saveChampions(anyString(), anyString(), anyCollection());
        verify(repository, never()).markReady(anyString(), anyString());
        verify(repository, never()).markFailed(anyString(), anyString());
        verify(repository, never()).activate(anyString());
    }

    @Test
    void localeSwitchIngestsActivePatchInNewLocale() {
        service = new PatchService(gameData, statistics, repository, new CatalogSettings(() -> "en_US", Duration.ofMinutes(15)));
        when(gameData.versions()).thenReturn(List.of(OLD));
        Patch active = patch(OLD, true, PT_BR, PatchStatus.READY);
        patches(active);
        stubChampionsIn(OLD, "en_US", "Ahri");

        service.syncLatest();

        verify(repository).tryLock(eq(OLD), eq("en_US"), any(), any());
        verify(repository).saveChampions(eq(OLD), eq("en_US"), anyCollection());
        verify(repository).markReady(OLD, "en_US");
        verify(repository, never()).activate(anyString());
    }

    @Test
    void localeSwitchWithNewerPatchFailingStillIngestsActivePatch() {
        service = new PatchService(gameData, statistics, repository, new CatalogSettings(() -> "en_US", Duration.ofMinutes(15)));
        patches(patch(OLD, true, PT_BR, PatchStatus.READY), patch(NEW, false));
        when(gameData.champions(NEW, "en_US")).thenThrow(new ExternalServiceUnavailableException("ddragon", "down"));
        stubChampionsIn(OLD, "en_US", "Ahri");

        service.syncLatest();

        verify(repository).markFailed(NEW, "en_US");
        verify(repository).markReady(OLD, "en_US");
        verify(repository, never()).activate(anyString());
    }

    @Test
    void detailsAreFetchedWithAtMostEightConcurrently() {
        patches(patch(NEW, false));
        when(gameData.versions()).thenReturn(List.of(NEW));
        String[] ids = new String[40];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = "Champ" + i;
        }
        Map<Long, Champion> champions = new LinkedHashMap<>();
        for (int i = 0; i < ids.length; i++) {
            champions.put((long) i, new Champion(ids[i], i, ids[i], "t", List.of()));
        }
        when(gameData.champions(NEW, PT_BR)).thenReturn(champions);
        AtomicInteger running = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        when(gameData.championDetail(eq(NEW), eq(PT_BR), anyString())).thenAnswer(i -> {
            peak.accumulateAndGet(running.incrementAndGet(), Math::max);
            Thread.sleep(30);
            running.decrementAndGet();
            return Optional.of(detail(i.getArgument(2)));
        });

        service.syncLatest();

        assertThat(peak.get()).isBetween(2, 8);
        verify(repository).markReady(NEW, PT_BR);
    }

    @Test
    void ingestRethrowsAndMarksFailed() {
        stubChampions(OLD, "Ahri");
        when(gameData.championDetail(OLD, PT_BR, "Ahri")).thenThrow(new ExternalServiceUnavailableException("ddragon", "x"));

        assertThatThrownBy(() -> service.ingest(OLD)).isInstanceOf(ExternalServiceUnavailableException.class);

        verify(repository).markFailed(OLD, PT_BR);
        verify(repository, never()).activate(anyString());
        verify(repository, never()).savePickRates(anyString(), any(), anyMap());
    }

    @Test
    void ingestOfOldPatchSkipsPickRates() {
        stubChampions(OLD, "Ahri");

        service.ingest(OLD);

        verify(repository).markReady(OLD, PT_BR);
        verify(repository, never()).savePickRates(anyString(), any(), anyMap());
        verify(repository, never()).activate(anyString());
    }

    @Test
    void ingestSkippedWhenLocked() {
        when(repository.tryLock(eq(OLD), anyString(), any(), any())).thenReturn(false);

        service.ingest(OLD);

        verify(repository).tryLock(eq(OLD), anyString(), any(), any());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void listsPatches() {
        List<Patch> all = List.of(patch(NEW, true), patch(OLD, false));
        when(repository.findAllPatches()).thenReturn(all);

        assertThat(service.execute()).isEqualTo(all);
    }

    private void patches(Patch... patches) {
        for (Patch patch : patches) {
            when(repository.findPatch(patch.version())).thenReturn(Optional.of(patch));
            if (patch.active()) {
                when(repository.findActivePatch()).thenReturn(Optional.of(patch));
            }
        }
    }

    private static Patch patch(String version, boolean active) {
        return new Patch(version, active, Map.of());
    }

    private static Patch patch(String version, boolean active, String locale, PatchStatus status) {
        return new Patch(version, active, Map.of(locale, status));
    }

    private void stubChampions(String version, String... ids) {
        stubChampionsIn(version, PT_BR, ids);
    }

    private void stubChampionsIn(String version, String locale, String... ids) {
        Map<Long, Champion> champions = new LinkedHashMap<>();
        long key = 1;
        for (String id : ids) {
            champions.put(key, new Champion(id, key, id, "title", List.of()));
            key++;
            when(gameData.championDetail(version, locale, id)).thenReturn(Optional.of(detail(id)));
        }
        when(gameData.champions(version, locale)).thenReturn(champions);
    }

    private static ChampionDetail detail(String id) {
        return new ChampionDetail(id, 1, id, "title", "lore", "blurb", List.of(), "Mana", null, null, List.of(),
                List.of(new ChampionDetail.Skin("103000", 0, "default"), new ChampionDetail.Skin("103001", 1, "Dynasty")),
                List.of(), List.of(), "icon");
    }
}
