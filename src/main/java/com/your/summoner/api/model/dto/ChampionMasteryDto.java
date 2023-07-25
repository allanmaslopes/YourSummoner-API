package com.your.summoner.api.model.dto;

import com.your.summoner.api.model.champion.ChampionByMastery;

public class ChampionMasteryDto {

    private Long key;

    private String championName;

    private Integer masteryPoints;

    private Integer masteryLevel;

    private Boolean chestWinned;

    private Double pickRate;

    public ChampionMasteryDto(ChampionByMastery championByMastery, ChampionDto champion) {
        this.key = championByMastery.getChampionId();
        this.championName = champion.getName();
        this.masteryPoints = championByMastery.getChampionPoints();
        this.masteryLevel = championByMastery.getChampionLevel();
        this.chestWinned = championByMastery.getChestGranted();
        this.pickRate = 0.0;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public Integer getMasteryPoints() {
        return masteryPoints;
    }

    public void setMasteryPoints(Integer masteryPoints) {
        this.masteryPoints = masteryPoints;
    }

    public Integer getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(Integer masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public Boolean getChestWinned() {
        return chestWinned;
    }

    public void setChestWinned(Boolean chestWinned) {
        this.chestWinned = chestWinned;
    }

    public Double getPickRate() {
        return pickRate;
    }

    public void setPickRate(Double pickRate) {
        this.pickRate = pickRate;
    }
}
