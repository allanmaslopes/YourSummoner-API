package com.yoursummoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChampionByMastery {

    @JsonProperty(namespace = "championId")
    private Long championId;

    @JsonProperty(namespace = "championLevel")
    private Integer championLevel;

    @JsonProperty(namespace = "championPoints")
    private Integer championPoints;

    @JsonProperty(namespace = "lastPlayTime")
    private Long lastPlayTime;

    @JsonProperty(namespace = "championPointsSinceLastLevel")
    private Long championPointsSinceLastLevel;

    @JsonProperty(namespace = "championPointsUntilNextLevel")
    private Long championPointsUntilNextLevel;

    @JsonProperty(namespace = "chestGranted")
    private Boolean chestGranted;

    @JsonProperty(namespace = "tokensEarned")
    private Integer tokensEarned;

    @JsonProperty(namespace = "summonerId")
    private String summonerId;

    public ChampionByMastery(Long championPointsUntilNextLevel, Boolean chestGranted, Long championId, Long lastPlayTime, Integer championLevel, String summonerId, Integer championPoints, Long championPointsSinceLastLevel, Integer tokensEarned) {
        this.championPointsUntilNextLevel = championPointsUntilNextLevel;
        this.chestGranted = chestGranted;
        this.championId = championId;
        this.lastPlayTime = lastPlayTime;
        this.championLevel = championLevel;
        this.summonerId = summonerId;
        this.championPoints = championPoints;
        this.championPointsSinceLastLevel = championPointsSinceLastLevel;
        this.tokensEarned = tokensEarned;
    }

    public Long getChampionId() {
        return championId;
    }

    public void setChampionId(Long championId) {
        this.championId = championId;
    }

    public Integer getChampionLevel() {
        return championLevel;
    }

    public void setChampionLevel(Integer championLevel) {
        this.championLevel = championLevel;
    }

    public Integer getChampionPoints() {
        return championPoints;
    }

    public void setChampionPoints(Integer championPoints) {
        this.championPoints = championPoints;
    }

    public Long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(Long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public Long getChampionPointsSinceLastLevel() {
        return championPointsSinceLastLevel;
    }

    public void setChampionPointsSinceLastLevel(Long championPointsSinceLastLevel) {
        this.championPointsSinceLastLevel = championPointsSinceLastLevel;
    }

    public Long getChampionPointsUntilNextLevel() {
        return championPointsUntilNextLevel;
    }

    public void setChampionPointsUntilNextLevel(Long championPointsUntilNextLevel) {
        this.championPointsUntilNextLevel = championPointsUntilNextLevel;
    }

    public Boolean getChestGranted() {
        return chestGranted;
    }

    public void setChestGranted(Boolean chestGranted) {
        this.chestGranted = chestGranted;
    }

    public Integer getTokensEarned() {
        return tokensEarned;
    }

    public void setTokensEarned(Integer tokensEarned) {
        this.tokensEarned = tokensEarned;
    }

    public String getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
    }
}
