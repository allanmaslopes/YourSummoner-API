package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.PlayerMatch;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;

import java.util.List;

/** Últimas partidas do jogador com o detalhe de cada uma, mais recente primeiro. */
public interface GetMatchHistoryUseCase {

    List<PlayerMatch> history(RiotId riotId, Platform platform, MatchQuery query);
}
