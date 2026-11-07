package com.your.summoner.infrastructure.adapter.in.rest.dto;

public record ChampionSkinResponse(String championId, String skinId, int num, String name, String splashUrl,
                                   String loadingUrl) {
}
