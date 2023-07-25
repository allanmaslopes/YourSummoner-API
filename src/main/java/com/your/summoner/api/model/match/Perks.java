package com.your.summoner.api.model.match;

import java.util.List;

public class Perks {

    private StatPerks statPerks;

    private List<Style> styles;

    public Perks(StatPerks statPerks, List<Style> styles) {
        this.statPerks = statPerks;
        this.styles = styles;
    }

    public StatPerks getStatPerks() {
        return statPerks;
    }

    public void setStatPerks(StatPerks statPerks) {
        this.statPerks = statPerks;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }
}
