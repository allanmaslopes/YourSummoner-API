package com.your.summoner.infrastructure.adapter.in.rest.dto;

import java.util.List;

public record MatchHistoryEntryResponse(String matchId, long gameCreation, long gameDuration, String gameMode,
                                        int queueId, int championId, String championName, String teamPosition,
                                        int champLevel, int kills, int deaths, int assists, double kda,
                                        int totalMinionsKilled, int goldEarned, int visionScore, boolean win,
                                        List<Integer> items) {
}
