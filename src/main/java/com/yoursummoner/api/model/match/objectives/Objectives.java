package com.yoursummoner.api.model.match.objectives;

public abstract class Objectives {

    private Boolean first;

    private Integer kills;

    public Objectives(Boolean first, Integer kills) {
        this.first = first;
        this.kills = kills;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Integer getKills() {
        return kills;
    }

    public void setKills(Integer kills) {
        this.kills = kills;
    }

}
