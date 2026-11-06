package com.your.summoner.application.service;

import com.your.summoner.application.port.in.GetSummonerProfileUseCase;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.application.port.out.LeagueProvider;
import com.your.summoner.application.port.out.SummonerProvider;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.MainChampion;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.domain.model.SummonerDetails;
import com.your.summoner.domain.model.SummonerProfile;
import com.your.summoner.domain.service.MasteryRanking;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummonerProfileService implements GetSummonerProfileUseCase {

    static final int MAIN_CHAMPIONS = 3;

    private final AccountResolver accountResolver;
    private final SummonerProvider summoners;
    private final LeagueProvider leagues;
    private final ChampionMasteryProvider masteries;
    private final ChampionCatalog catalog;

    public SummonerProfileService(AccountResolver accountResolver, SummonerProvider summoners,
                                  LeagueProvider leagues, ChampionMasteryProvider masteries,
                                  ChampionCatalog catalog) {
        this.accountResolver = accountResolver;
        this.summoners = summoners;
        this.leagues = leagues;
        this.masteries = masteries;
        this.catalog = catalog;
    }

    @Override
    public SummonerProfile execute(RiotId riotId, Platform platform) {
        Account account = accountResolver.resolve(riotId, platform);

        // A conta existe globalmente, mas pode não ter invocador na plataforma pedida.
        SummonerDetails summoner = summoners.findByPuuid(account.puuid(), platform)
                .orElseThrow(() -> new ResourceNotFoundException("Summoner on " + platform, riotId.toString()));

        String patch = catalog.activePatch();
        var ranked = MasteryRanking.merge(masteries.findByPuuid(account.puuid(), platform), catalog.champions(patch));
        List<MainChampion> mainChampions = MasteryRanking.top(ranked, MAIN_CHAMPIONS).stream()
                .map(m -> new MainChampion(m, catalog.championIconUrl(patch, m.champion().id())))
                .toList();

        return new SummonerProfile(account, summoner, catalog.profileIconUrl(patch, summoner.profileIconId()),
                leagues.findByPuuid(account.puuid(), platform), mainChampions);
    }
}
