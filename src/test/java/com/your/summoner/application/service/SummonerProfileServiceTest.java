package com.your.summoner.application.service;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.application.port.out.LeagueProvider;
import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.LeagueEntry;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.domain.model.SummonerDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SummonerProfileServiceTest {

    private static final RiotId RIOT_ID = new RiotId("Tester", "BR1");
    private static final Account ACCOUNT = new Account("puuid-1", "Tester", "BR1");
    private static final String PATCH = "16.21.1";

    private final AccountProvider accounts = mock(AccountProvider.class);
    private final SummonerProvider summoners = mock(SummonerProvider.class);
    private final LeagueProvider leagues = mock(LeagueProvider.class);
    private final ChampionMasteryProvider masteries = mock(ChampionMasteryProvider.class);
    private final ChampionCatalog catalog = mock(ChampionCatalog.class);

    private SummonerProfileService service;

    @BeforeEach
    void setUp() {
        service = new SummonerProfileService(new AccountResolver(accounts), summoners, leagues, masteries, catalog);
        when(catalog.activePatch()).thenReturn(PATCH);
        when(catalog.profileIconUrl(anyString(), anyInt())).thenAnswer(i -> "icon/" + i.getArgument(1));
        when(catalog.championIconUrl(anyString(), anyString())).thenAnswer(i -> "champ/" + i.getArgument(1));
        when(catalog.champions(PATCH)).thenReturn(Map.of(
                1L, new Champion("Annie", 1, "Annie", "", List.of("Mage")),
                2L, new Champion("Olaf", 2, "Olaf", "", List.of("Fighter")),
                3L, new Champion("Galio", 3, "Galio", "", List.of("Tank")),
                4L, new Champion("TwistedFate", 4, "Twisted Fate", "", List.of("Mage"))));
    }

    @Test
    void buildsTheProfileWithTheTopThreeChampions() {
        when(accounts.findByRiotId(RIOT_ID, Region.AMERICAS)).thenReturn(Optional.of(ACCOUNT));
        when(summoners.findByPuuid("puuid-1", Platform.BR1)).thenReturn(Optional.of(new SummonerDetails("puuid-1", 29, 300, 0)));
        when(leagues.findByPuuid("puuid-1", Platform.BR1)).thenReturn(List.of(
                new LeagueEntry("RANKED_SOLO_5x5", "GOLD", "I", 10, 6, 4, false)));
        when(masteries.findByPuuid("puuid-1", Platform.BR1)).thenReturn(List.of(
                new ChampionMastery(1, 5, 100, 0), new ChampionMastery(2, 5, 400, 0),
                new ChampionMastery(3, 5, 300, 0), new ChampionMastery(4, 5, 200, 0)));

        var profile = service.execute(RIOT_ID, Platform.BR1);

        assertEquals(ACCOUNT, profile.account());
        assertEquals(300, profile.summoner().summonerLevel());
        assertEquals("icon/29", profile.profileIconUrl());
        assertEquals(1, profile.rankings().size());
        assertEquals(List.of("Olaf", "Galio", "TwistedFate"),
                profile.mainChampions().stream().map(m -> m.champion().champion().id()).toList());
        assertEquals("champ/Olaf", profile.mainChampions().getFirst().iconUrl());
    }

    @Test
    void usesTheAccountRegionOfThePlatform() {
        when(accounts.findByRiotId(RIOT_ID, Region.ASIA)).thenReturn(Optional.of(ACCOUNT));
        when(summoners.findByPuuid("puuid-1", Platform.VN2)).thenReturn(Optional.of(new SummonerDetails("puuid-1", 1, 1, 0)));
        when(leagues.findByPuuid(any(), any())).thenReturn(List.of());
        when(masteries.findByPuuid(any(), any())).thenReturn(List.of());

        var profile = service.execute(RIOT_ID, Platform.VN2);

        assertEquals(0, profile.mainChampions().size());
        verify(accounts).findByRiotId(RIOT_ID, Region.ASIA);
    }

    @Test
    void unknownAccountIsNotFound() {
        when(accounts.findByRiotId(RIOT_ID, Region.AMERICAS)).thenReturn(Optional.empty());

        var e = assertThrows(ResourceNotFoundException.class, () -> service.execute(RIOT_ID, Platform.BR1));

        assertEquals("Summoner", e.resource());
        verify(summoners, never()).findByPuuid(any(), any());
    }

    @Test
    void accountWithoutSummonerOnThePlatformIsNotFound() {
        when(accounts.findByRiotId(RIOT_ID, Region.AMERICAS)).thenReturn(Optional.of(ACCOUNT));
        when(summoners.findByPuuid("puuid-1", Platform.NA1)).thenReturn(Optional.empty());

        var e = assertThrows(ResourceNotFoundException.class, () -> service.execute(RIOT_ID, Platform.NA1));

        assertEquals("Summoner on NA1", e.resource());
    }
}
