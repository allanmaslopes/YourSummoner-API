package com.your.summoner.infrastructure.adapter.out.riot.dto;

public record RiotChampionMasteryDto(long championId, int championLevel, long championPoints, long lastPlayTime) {
}
