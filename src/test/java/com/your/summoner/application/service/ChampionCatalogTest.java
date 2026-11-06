package com.your.summoner.application.service;

import com.your.summoner.application.port.in.SyncPatchesUseCase;
import com.your.summoner.application.port.out.CatalogStorageUnavailableException;
import com.your.summoner.application.port.out.ChampionStatisticsProvider;
import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.application.port.out.StaticDataRepository;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import com.your.summoner.domain.model.PatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChampionCatalogTest {

    private static final String ACTIVE = "14.2.1";
    private static final String OLD = "13.1.1";
    private static final String PT_BR = "pt_BR";
    private static final Champion AHRI = new Champion("Ahri", 103, "Ahri", "title", List.of());

    @Mock StaticDataRepository repository;
    @Mock GameDataProvider gameData;
    @Mock ChampionStatisticsProvider statistics;
    @Mock SyncPatchesUseCase sync;

    private ChampionCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new ChampionCatalog(repository, gameData, statistics, sync,
                new CatalogSettings(() -> PT_BR, Duration.ofMinutes(15)));
        when(repository.findActivePatch()).thenReturn(Optional.of(ready(ACTIVE, true)));
        when(repository.findPatch(ACTIVE)).thenReturn(Optional.of(ready(ACTIVE, true)));
    }

    @Test
    void readsFromMongoWhenReady() {
        when(repository.findChampions(ACTIVE, PT_BR)).thenReturn(Map.of(103L, AHRI));

        assertThat(catalog.champions(ACTIVE)).containsEntry(103L, AHRI);

        verifyNoInteractions(gameData, sync);
    }

    @Test
    void oldPatchIsIngestedOnDemand() {
        when(repository.findPatch(OLD)).thenReturn(Optional.of(new Patch(OLD, false, Map.of())));
        when(repository.findChampions(OLD, PT_BR)).thenReturn(Map.of(), Map.of(103L, AHRI));

        assertThat(catalog.champions(OLD)).containsEntry(103L, AHRI);

        verify(sync).ingest(OLD);
        verify(gameData, never()).champions(anyString(), anyString());
    }

    @Test
    void ingestionHeldByAnotherInstanceFallsBackToDataDragon() {
        when(repository.findPatch(OLD)).thenReturn(Optional.of(new Patch(OLD, false, Map.of())));
        when(repository.findChampions(OLD, PT_BR)).thenReturn(Map.of());
        when(gameData.champions(OLD, PT_BR)).thenReturn(Map.of(103L, AHRI));

        assertThat(catalog.champions(OLD)).containsEntry(103L, AHRI);
    }

    @Test
    void unknownPatchIsNotFoundWithoutIngestion() {
        when(repository.findPatch("99.99.1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalog.resolve("99.99.1")).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> catalog.resolve("abc")).isInstanceOf(ResourceNotFoundException.class);

        verify(sync, never()).ingest(anyString());
        verify(repository, never()).findPatch("abc");
    }

    @Test
    void resolveWithoutPatchReturnsActive() {
        assertThat(catalog.resolve(null)).isEqualTo(ACTIVE);
        assertThat(catalog.resolve("  ")).isEqualTo(ACTIVE);
        assertThat(catalog.resolve(ACTIVE)).isEqualTo(ACTIVE);
    }

    @Test
    void mongoDownFallsBackToDataDragon() {
        var down = new CatalogStorageUnavailableException("down", new RuntimeException());
        when(repository.findActivePatch()).thenThrow(down);
        when(repository.findPatch(anyString())).thenThrow(down);
        when(repository.findChampions(anyString(), anyString())).thenThrow(down);
        when(repository.findChampionDetail(anyString(), anyString(), anyString())).thenThrow(down);
        when(repository.findSkins(anyString(), anyString(), anyString())).thenThrow(down);
        when(repository.findPickRates(anyString(), org.mockito.ArgumentMatchers.any())).thenThrow(down);
        when(gameData.versions()).thenReturn(List.of(ACTIVE, OLD));
        when(gameData.champions(ACTIVE, PT_BR)).thenReturn(Map.of(103L, AHRI));
        when(gameData.championDetail(ACTIVE, PT_BR, "Ahri")).thenReturn(Optional.of(detail()));
        when(gameData.skinSplashUrl("Ahri", 0)).thenReturn("splash");
        when(gameData.skinLoadingUrl("Ahri", 0)).thenReturn("loading");
        when(statistics.pickRates(Lane.MIDDLE)).thenReturn(Map.of(103L, 0.1));

        assertThat(catalog.activePatch()).isEqualTo(ACTIVE);
        assertThat(catalog.resolve(OLD)).isEqualTo(OLD);
        assertThatThrownBy(() -> catalog.resolve("99.99.1")).isInstanceOf(ResourceNotFoundException.class);
        assertThat(catalog.champions(ACTIVE)).containsEntry(103L, AHRI);
        assertThat(catalog.championDetail(ACTIVE, "Ahri")).contains(detail());
        assertThat(catalog.skins(ACTIVE, "Ahri")).containsExactly(
                new ChampionSkin("Ahri", "103000", 0, "default", "splash", "loading"));
        assertThat(catalog.pickRates(Lane.MIDDLE)).containsEntry(103L, 0.1);
        verifyNoInteractions(sync);
    }

    @Test
    void noCatalogAtAllIsServiceUnavailable() {
        when(repository.findActivePatch()).thenReturn(Optional.empty());
        when(gameData.versions()).thenThrow(new ExternalServiceUnavailableException("ddragon", "down"));

        assertThatThrownBy(() -> catalog.activePatch()).isInstanceOf(ExternalServiceUnavailableException.class);

        when(repository.findChampions(ACTIVE, PT_BR)).thenReturn(Map.of());
        when(repository.findPatch(ACTIVE)).thenReturn(Optional.empty());
        when(gameData.champions(ACTIVE, PT_BR)).thenThrow(new ExternalServiceUnavailableException("ddragon", "down"));

        assertThatThrownBy(() -> catalog.champions(ACTIVE)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void emptyResultOfReadyPatchIsAuthoritative() {
        when(repository.findChampionDetail(ACTIVE, PT_BR, "Nobody")).thenReturn(Optional.empty());

        assertThat(catalog.championDetail(ACTIVE, "Nobody")).isEmpty();

        verifyNoInteractions(gameData, sync);
    }

    @Test
    void localeChangeDuringReadDoesNotMakeAnotherLocaleAuthoritative() {
        AtomicReference<String> locale = new AtomicReference<>("en_US");
        catalog = new ChampionCatalog(repository, gameData, statistics, sync,
                new CatalogSettings(locale::get, Duration.ofMinutes(15)));
        when(repository.findChampionDetail(ACTIVE, "en_US", "Ahri")).thenAnswer(invocation -> {
            locale.set(PT_BR);
            return Optional.empty();
        });
        when(gameData.championDetail(ACTIVE, "en_US", "Ahri")).thenReturn(Optional.of(detail()));

        assertThat(catalog.championDetail(ACTIVE, "Ahri").map(ChampionDetail::id)).contains("Ahri");
    }

    @Test
    void pickRatesComeFromActivePatchSnapshot() {
        when(repository.findPickRates(ACTIVE, Lane.TOP)).thenReturn(Optional.of(Map.of(1L, 0.2)));

        assertThat(catalog.pickRates(Lane.TOP)).containsEntry(1L, 0.2);

        verifyNoInteractions(statistics);
    }

    private static Patch ready(String version, boolean active) {
        return new Patch(version, active, Map.of(PT_BR, PatchStatus.READY));
    }

    private static ChampionDetail detail() {
        return new ChampionDetail("Ahri", 103, "Ahri", "t", "l", "b", List.of(), "Mana", null, null, List.of(),
                List.of(new ChampionDetail.Skin("103000", 0, "default")), List.of(), List.of(), "icon");
    }
}
