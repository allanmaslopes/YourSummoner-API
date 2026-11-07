package com.your.summoner.infrastructure.adapter.in.rest.dto;

import java.util.List;

public record MatchResponse(String matchId, String platformId, long gameCreation, long gameDuration,
                            String gameMode, String gameType, String gameVersion, int queueId, int mapId,
                            List<Participant> participants, List<Team> teams) {

    public record Participant(String puuid, String riotIdGameName, String riotIdTagline, int teamId,
                              int championId, String championName, String teamPosition, int champLevel,
                              int kills, int deaths, int assists, double kda, int totalMinionsKilled,
                              int neutralMinionsKilled, int goldEarned, int totalDamageDealtToChampions,
                              int visionScore, boolean win, List<Integer> items, int summoner1Id,
                              int summoner2Id, int playerSubteamId, int subteamPlacement) {
    }

    public record Team(int teamId, boolean win, List<Integer> bannedChampionIds, int baronKills,
                       int dragonKills, int towerKills, int championKills) {
    }
}
