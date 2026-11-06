package com.your.summoner.domain.model;

import java.util.Locale;

/** Posição ranqueada em uma fila (league-v4). */
public record LeagueEntry(String queueType, String tier, String rank, int leaguePoints,
                          int wins, int losses, boolean hotStreak) {

    public int totalMatches() {
        return wins + losses;
    }

    /** Percentual de vitórias arredondado (0 quando não há partidas). */
    public double winRate() {
        int total = totalMatches();
        return total == 0 ? 0 : Math.round(wins * 100.0 / total);
    }

    /** {@code CHALLENGER} → {@code Challenger}. */
    public String displayTier() {
        if (tier == null || tier.isEmpty()) {
            return "";
        }
        return tier.charAt(0) + tier.substring(1).toLowerCase(Locale.ROOT);
    }
}
