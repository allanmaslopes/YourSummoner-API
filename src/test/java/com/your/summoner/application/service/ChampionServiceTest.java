package com.your.summoner.application.service;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChampionServiceTest {

    private static final String PATCH = "16.21.1";

    private final AccountProvider accounts = mock(AccountProvider.class);
    private final ChampionMasteryProvider masteries = mock(ChampionMasteryProvider.class);
    private final ChampionCatalog catalog = mock(ChampionCatalog.class);

    private ChampionService service;
    private ChampionSkinService skinService;

    @BeforeEach
    void setUp() {
        service = new ChampionService(new AccountResolver(accounts), masteries, catalog);
        skinService = new ChampionSkinService(catalog);
        when(catalog.activePatch()).thenReturn(PATCH);
        when(catalog.resolve(any())).thenAnswer(i -> i.getArgument(0) == null ? PATCH : i.getArgument(0));
        when(catalog.champions(PATCH)).thenReturn(Map.of(
                62L, new Champion("MonkeyKing", 62, "Wukong", "", List.of("Fighter")),
                103L, new Champion("Ahri", 103, "Ahri", "", List.of("Mage")),
                145L, new Champion("Kaisa", 145, "Kai'Sa", "", List.of("Marksman")),
                21L, new Champion("MissFortune", 21, "Miss Fortune", "", List.of("Marksman"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MonkeyKing", "monkeyking", "Wukong", " wukong "})
    void resolvesChampionByIdOrName(String name) {
        var detail = detail("MonkeyKing");
        when(catalog.championDetail(PATCH, "MonkeyKing")).thenReturn(Optional.of(detail));

        assertEquals(detail, service.execute(name, null));
    }

    @ParameterizedTest
    @CsvSource({"ahri,Ahri", "AHRI,Ahri", "Kai'Sa,Kaisa", "kai'sa,Kaisa", "Kaisa,Kaisa",
            "Miss Fortune,MissFortune", "miss fortune,MissFortune", "missfortune,MissFortune"})
    void resolvesNameCaseInsensitively(String name, String expectedId) {
        var detail = detail(expectedId);
        when(catalog.championDetail(PATCH, expectedId)).thenReturn(Optional.of(detail));

        assertEquals(expectedId, service.execute(name, null).id());
    }

    @Test
    void unknownChampionIsNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.execute("Nope", null));
    }

    @Test
    void blankChampionIsInvalid() {
        assertThrows(InvalidInputException.class, () -> service.execute(" ", null));
    }

    @Test
    void unknownPatchPropagatesNotFoundWithoutReadingTheCatalog() {
        when(catalog.resolve("99.99.1")).thenThrow(new ResourceNotFoundException("Patch", "99.99.1"));

        assertThrows(ResourceNotFoundException.class, () -> service.execute("Ahri", "99.99.1"));
        verify(catalog, never()).champions(any());
    }

    @Test
    void laneChampionsCarryThePickRate() {
        var riotId = new RiotId("Tester", "BR1");
        when(accounts.findByRiotId(riotId, Region.AMERICAS)).thenReturn(Optional.of(new Account("p", "Tester", "BR1")));
        when(masteries.findByPuuid("p", Platform.BR1)).thenReturn(List.of(
                new ChampionMastery(62, 7, 1000, 0), new ChampionMastery(103, 7, 2000, 0)));
        when(catalog.pickRates(Lane.TOP)).thenReturn(Map.of(62L, 0.01));

        var result = service.execute(riotId, Platform.BR1, Lane.TOP, null);

        assertEquals(1, result.size());
        assertEquals("MonkeyKing", result.getFirst().champion().id());
        assertEquals(1.0, result.getFirst().pickRate());
    }

    @Test
    void masteriesUseTheRequestedPatchCatalog() {
        var riotId = new RiotId("Tester", "BR1");
        when(accounts.findByRiotId(riotId, Region.AMERICAS)).thenReturn(Optional.of(new Account("p", "Tester", "BR1")));
        when(masteries.findByPuuid("p", Platform.BR1)).thenReturn(List.of(new ChampionMastery(103, 7, 2000, 0)));
        when(catalog.champions("16.20.1")).thenReturn(Map.of(
                103L, new Champion("Ahri", 103, "Ahri Old", "", List.of("Mage"))));

        var result = service.execute(riotId, Platform.BR1, "16.20.1");

        assertEquals("Ahri Old", result.getFirst().champion().name());
    }

    @Test
    void returnsTheActivePatch() {
        assertEquals(PATCH, service.execute());
    }

    @Test
    void skinsResolveTheChampionByName() {
        var skin = new ChampionSkin("Kaisa", "145000", 0, "Kai'Sa", "splash", "loading");
        when(catalog.skins(PATCH, "Kaisa")).thenReturn(List.of(skin));

        assertEquals(List.of(skin), skinService.execute("kai'sa", null));
    }

    private static ChampionDetail detail(String id) {
        return new ChampionDetail(id, 62, "Wukong", "", "", "", List.of(), "Mana", null, null,
                List.of(), List.of(), List.of(), List.of(), "url");
    }
}
