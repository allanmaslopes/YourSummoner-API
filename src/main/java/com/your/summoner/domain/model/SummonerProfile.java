package com.your.summoner.domain.model;

import java.util.List;

/** Perfil agregado: conta + invocador + ranqueadas + campeões principais. */
public record SummonerProfile(Account account, SummonerDetails summoner, String profileIconUrl,
                              List<LeagueEntry> rankings, List<MainChampion> mainChampions) {

    public SummonerProfile {
        rankings = List.copyOf(rankings);
        mainChampions = List.copyOf(mainChampions);
    }
}
