package com.your.summoner.domain.model;

/** Dados do invocador em uma plataforma (summoner-v4). */
public record SummonerDetails(String puuid, int profileIconId, long summonerLevel, long revisionDate) {
}
