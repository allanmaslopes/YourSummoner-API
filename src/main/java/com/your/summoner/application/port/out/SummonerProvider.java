package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.SummonerDetails;

import java.util.Optional;

/** Dados do invocador por plataforma (summoner-v4). */
public interface SummonerProvider {

    Optional<SummonerDetails> findByPuuid(String puuid, Platform platform);
}
