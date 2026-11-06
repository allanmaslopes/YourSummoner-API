package com.your.summoner.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * Maestria de um campeão. {@code pickRate} (percentual) só vem preenchido na busca por lane.
 * O antigo {@code chestWinned} saiu: a Riot removeu {@code chestGranted} da champion-mastery-v4.
 */
public record ChampionMasteryResponse(String id, long key, String championName, long masteryPoints,
                                      int masteryLevel, long lastPlayTime, Double pickRate, List<String> tags) {
}
