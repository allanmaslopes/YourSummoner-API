package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.Match;

public interface GetMatchUseCase {

    /** A plataforma (e portanto a região) sai do prefixo do id: {@code BR1_123}. */
    Match execute(String matchId);
}
