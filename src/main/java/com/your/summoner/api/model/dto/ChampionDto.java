package com.your.summoner.api.model.dto;

import com.your.summoner.api.model.champion.Champion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChampionDto {

    private String id;

    private String key;

    private String name;

    private List<String> tags;

    public ChampionDto(Champion champion) {
        this.id = champion.getId();
        this.key = champion.getKey();
        this.name = champion.getName();
        this.tags = champion.getTags();
    }
}
