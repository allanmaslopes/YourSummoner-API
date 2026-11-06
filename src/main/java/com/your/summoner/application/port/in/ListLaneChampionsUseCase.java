package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.MasteredChampion;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;

import java.util.List;

public interface ListLaneChampionsUseCase {

    /** Campeões do jogador jogados na lane, com o pick rate global. {@code patch} nulo vale o patch ativo. */
    List<MasteredChampion> execute(RiotId riotId, Platform platform, Lane lane, String patch);
}
