package com.your.summoner.domain.model;

/** Maestria de um campeão para um jogador (champion-mastery-v4). */
public record ChampionMastery(long championId, int championLevel, long championPoints, long lastPlayTime) {
}
