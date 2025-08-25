package com.your.summoner.api.model.champion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Skin {

    @JsonProperty
    private String id;

    @JsonProperty
    private int num;

    @JsonProperty
    private String name;

    @JsonProperty
    private boolean chromas;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChromas() {
        return chromas;
    }

    public void setChromas(boolean chromas) {
        this.chromas = chromas;
    }
}
