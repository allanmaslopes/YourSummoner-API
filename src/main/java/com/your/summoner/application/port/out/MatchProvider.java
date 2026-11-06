package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Region;

import java.util.List;
import java.util.Optional;

/** Histórico de partidas (match-v5). */
public interface MatchProvider {

    List<String> findMatchIds(String puuid, Region region, MatchQuery query);

    Optional<Match> findById(String matchId, Region region);
}
