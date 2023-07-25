package com.your.summoner.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ranking {
    public String leagueId;
    public String queueType;
    public String tier;
    public String rank;
    public String summonerId;
    public String summonerName;
    public int leaguePoints;
    public int wins;
    public int losses;
    public boolean veteran;
    public boolean inactive;
    public boolean freshBlood;
    public boolean hotStreak;
}
