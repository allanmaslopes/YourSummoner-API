package com.your.summoner.api.model.match;

public class StatPerks {

    private Integer defense;

    private Integer flex;

    private Integer offense;

    public StatPerks(Integer defense, Integer flex, Integer offense) {
        this.defense = defense;
        this.flex = flex;
        this.offense = offense;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public Integer getFlex() {
        return flex;
    }

    public void setFlex(Integer flex) {
        this.flex = flex;
    }

    public Integer getOffense() {
        return offense;
    }

    public void setOffense(Integer offense) {
        this.offense = offense;
    }
}
