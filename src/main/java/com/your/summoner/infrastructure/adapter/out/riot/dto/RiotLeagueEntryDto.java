package com.your.summoner.infrastructure.adapter.out.riot.dto;

public record RiotLeagueEntryDto(String queueType, String tier, String rank, int leaguePoints,
                                 int wins, int losses, boolean hotStreak) {
}
