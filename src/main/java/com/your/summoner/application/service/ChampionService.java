package com.your.summoner.application.service;

import com.your.summoner.application.port.in.GetChampionDetailUseCase;
import com.your.summoner.application.port.in.GetGameVersionUseCase;
import com.your.summoner.application.port.in.ListChampionMasteriesUseCase;
import com.your.summoner.application.port.in.ListLaneChampionsUseCase;
import com.your.summoner.application.port.out.ChampionMasteryProvider;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.MasteredChampion;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.domain.service.MasteryRanking;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChampionService implements ListChampionMasteriesUseCase, ListLaneChampionsUseCase,
        GetChampionDetailUseCase, GetGameVersionUseCase {

    private final AccountResolver accountResolver;
    private final ChampionMasteryProvider masteries;
    private final ChampionCatalog catalog;

    public ChampionService(AccountResolver accountResolver, ChampionMasteryProvider masteries,
                           ChampionCatalog catalog) {
        this.accountResolver = accountResolver;
        this.masteries = masteries;
        this.catalog = catalog;
    }

    @Override
    public List<MasteredChampion> execute(RiotId riotId, Platform platform, String patch) {
        String resolved = catalog.resolve(patch);
        Account account = accountResolver.resolve(riotId, platform);
        return MasteryRanking.merge(masteries.findByPuuid(account.puuid(), platform), catalog.champions(resolved));
    }

    @Override
    public List<MasteredChampion> execute(RiotId riotId, Platform platform, Lane lane, String patch) {
        return MasteryRanking.forLane(execute(riotId, platform, patch), catalog.pickRates(lane));
    }

    @Override
    public ChampionDetail execute(String championName, String patch) {
        String resolved = catalog.resolve(patch);
        String championId = ChampionNames.resolveId(catalog, resolved, championName);
        return catalog.championDetail(resolved, championId)
                .orElseThrow(() -> new ResourceNotFoundException("Champion", championName.trim()));
    }

    @Override
    public String execute() {
        return catalog.activePatch();
    }
}
