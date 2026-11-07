package com.your.summoner.domain.model;

/**
 * Maestria do jogador enriquecida com o catálogo de campeões e, opcionalmente, o pick rate
 * (percentual) da lane.
 */
public record MasteredChampion(Champion champion, ChampionMastery mastery, Double pickRate) {

    public MasteredChampion withPickRate(double value) {
        return new MasteredChampion(champion, mastery, value);
    }
}
