package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;

import java.util.List;

public interface ListMatchIdsUseCase {

    List<String> execute(RiotId riotId, Platform platform, MatchQuery query);
}
