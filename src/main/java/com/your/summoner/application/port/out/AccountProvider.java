package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;

import java.util.Optional;

/** Busca de contas Riot (account-v1). */
public interface AccountProvider {

    Optional<Account> findByRiotId(RiotId riotId, Region region);
}
