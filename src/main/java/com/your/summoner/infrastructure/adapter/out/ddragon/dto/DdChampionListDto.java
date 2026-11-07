package com.your.summoner.infrastructure.adapter.out.ddragon.dto;

import java.util.List;
import java.util.Map;

/** {@code champion.json}: {@code {"data":{"Aatrox":{...}}}}. */
public record DdChampionListDto(String version, Map<String, Champion> data) {

    public record Champion(String id, String key, String name, String title, List<String> tags) {
    }
}
