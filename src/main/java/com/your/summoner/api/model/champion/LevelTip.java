package com.your.summoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LevelTip {

    @JsonProperty
    private List<String> label;

    @JsonProperty
    private List<String> effect;

    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }

    public List<String> getEffect() {
        return effect;
    }

    public void setEffect(List<String> effect) {
        this.effect = effect;
    }
}
