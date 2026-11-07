package com.your.summoner.support;

import static com.your.summoner.support.RiotApiWireMock.RIOT;

/** Os adapters da Riot com o path do stub default de cada um (BR1 / AMERICAS). */
public enum RiotEndpoint {
    SUMMONER(RIOT + "/br1/lol/summoner/v4/summoners/by-puuid/puuid-tester"),
    ACCOUNT(RIOT + "/americas/riot/account/v1/accounts/by-riot-id/Tester/BR1"),
    LEAGUE(RIOT + "/br1/lol/league/v4/entries/by-puuid/puuid-tester"),
    MASTERY(RIOT + "/br1/lol/champion-mastery/v4/champion-masteries/by-puuid/puuid-tester"),
    MATCH_IDS(RIOT + "/americas/lol/match/v5/matches/by-puuid/puuid-tester/ids"),
    MATCH(RIOT + "/americas/lol/match/v5/matches/BR1_3000000001");

    private final String path;

    RiotEndpoint(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
