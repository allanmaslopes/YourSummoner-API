package com.your.summoner.infrastructure.adapter.out.riot.dto;

import java.util.List;

/** Subconjunto do MatchDto da match-v5; campos desconhecidos são ignorados pelo Jackson. */
public record RiotMatchDto(Metadata metadata, Info info) {

    public record Metadata(String matchId) {
    }

    public record Info(long gameCreation, long gameDuration, String gameMode, String gameType,
                       String gameVersion, int queueId, int mapId, String platformId,
                       List<Participant> participants, List<Team> teams) {
    }

    public record Participant(String puuid, String riotIdGameName, String riotIdTagline, int teamId,
                              int championId, String championName, String teamPosition, int champLevel,
                              int kills, int deaths, int assists, int totalMinionsKilled,
                              int neutralMinionsKilled, int goldEarned, int totalDamageDealtToChampions,
                              int visionScore, boolean win, int item0, int item1, int item2, int item3,
                              int item4, int item5, int item6, int summoner1Id, int summoner2Id,
                              Integer playerSubteamId, Integer subteamPlacement) {
    }

    public record Team(int teamId, boolean win, List<Ban> bans, Objectives objectives) {
    }

    public record Ban(int championId, int pickTurn) {
    }

    public record Objectives(Objective baron, Objective dragon, Objective tower, Objective champion) {
    }

    public record Objective(boolean first, int kills) {
    }
}
