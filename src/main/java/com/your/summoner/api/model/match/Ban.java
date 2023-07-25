package com.your.summoner.api.model.match;

public class Ban {

    private Integer championId;

    private Integer pickTurn;

    public Ban(Integer championId, Integer pickTurn) {
        this.championId = championId;
        this.pickTurn = pickTurn;
    }

    public Integer getChampionId() {
        return championId;
    }

    public void setChampionId(Integer championId) {
        this.championId = championId;
    }

    public Integer getPickTurn() {
        return pickTurn;
    }

    public void setPickTurn(Integer pickTurn) {
        this.pickTurn = pickTurn;
    }
}
