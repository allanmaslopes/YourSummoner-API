package com.your.summoner.api.model.Dto;

import com.your.summoner.api.model.champion.Champion;

import java.util.List;

public class ChampionDto {

    private String key;

    private String name;

    private List<String> tags;

    public ChampionDto(Champion champion) {
        this.key = champion.getKey();
        this.name = champion.getName();
        this.tags = champion.getTags();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
