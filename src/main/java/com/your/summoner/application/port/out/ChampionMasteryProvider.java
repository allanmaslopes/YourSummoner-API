package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.Platform;

import java.util.List;

/** Maestrias do jogador (champion-mastery-v4). */
public interface ChampionMasteryProvider {

    List<ChampionMastery> findByPuuid(String puuid, Platform platform);
}
