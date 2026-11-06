package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.LeagueEntry;
import com.your.summoner.domain.model.Platform;

import java.util.List;

/** Filas ranqueadas do jogador (league-v4). */
public interface LeagueProvider {

    List<LeagueEntry> findByPuuid(String puuid, Platform platform);
}
