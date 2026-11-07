package com.your.summoner.application.service;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.RiotId;
import org.springframework.stereotype.Service;

/** Riot ID → conta (PUUID). Primeiro passo de quase todos os casos de uso. */
@Service
public class AccountResolver {

    private final AccountProvider accounts;

    public AccountResolver(AccountProvider accounts) {
        this.accounts = accounts;
    }

    public Account resolve(RiotId riotId, Platform platform) {
        return accounts.findByRiotId(riotId, platform.accountRegion())
                .orElseThrow(() -> new ResourceNotFoundException("Summoner", riotId.toString()));
    }
}
