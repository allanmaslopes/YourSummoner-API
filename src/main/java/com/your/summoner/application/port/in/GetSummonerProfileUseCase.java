package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.domain.model.SummonerProfile;

public interface GetSummonerProfileUseCase {

    SummonerProfile execute(RiotId riotId, Platform platform);
}
