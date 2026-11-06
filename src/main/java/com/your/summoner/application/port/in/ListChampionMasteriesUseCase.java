package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.MasteredChampion;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;

import java.util.List;

public interface ListChampionMasteriesUseCase {

    /** Todas as maestrias do jogador, ordenadas por pontos. {@code patch} nulo vale o patch ativo. */
    List<MasteredChampion> execute(RiotId riotId, Platform platform, String patch);
}
