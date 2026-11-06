package com.your.summoner.application.port.out;

import java.util.Optional;

/** Current Riot credentials, resolved for each outgoing request. */
public interface RiotCredentials {
    Optional<String> riotApiKey();
}
