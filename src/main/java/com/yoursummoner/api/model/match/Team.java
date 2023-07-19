package com.yoursummoner.api.model.match;

import com.yoursummoner.api.model.match.objectives.Objectives;

import java.util.List;

public class Team {

    private List<Ban> bans;

    private List<Objectives> objectives;

    private Integer teamId;

    private Boolean win;

    public Team(List<Ban> bans, List<Objectives> objectives, Integer teamId, Boolean win) {
        this.bans = bans;
        this.objectives = objectives;
        this.teamId = teamId;
        this.win = win;
    }

    public List<Ban> getBans() {
        return bans;
    }

    public void setBans(List<Ban> bans) {
        this.bans = bans;
    }

    public List<Objectives> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<Objectives> objectives) {
        this.objectives = objectives;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }
}
