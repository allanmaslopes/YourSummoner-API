package com.your.summoner.infrastructure.adapter.in.rest.dto;

import java.util.List;

/** Mantém os nomes de campo da API Spring (summonerName, urlIcon, rankingList...). */
public record SummonerProfileResponse(String summonerName, String tagLine, String puuid, String urlIcon,
                                      long level, List<Ranking> rankingList,
                                      List<MainChampionResponse> mainChampionList) {

    public record Ranking(String queueType, String tier, String rank, int leaguePoints, int wins, int losses,
                          int quantMatchs, boolean hotStreak, double winRate) {
    }

    public record MainChampionResponse(String id, long key, String championName, long masteryPoints,
                                       int masteryLevel, String urlIcon, List<String> tags) {
    }
}
