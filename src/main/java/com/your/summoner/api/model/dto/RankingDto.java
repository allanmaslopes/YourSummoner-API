package com.your.summoner.api.model.dto;

import com.your.summoner.api.model.Ranking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingDto {

    private String queueType;

    private String tier;

    private String rank;

    private int leaguePoints;

    private int wins;

    private int losses;

    private int quantMatchs;

    private boolean hotStreak;

    private double winRate;

    public RankingDto(Ranking ranking) {
        this.queueType = ranking.getQueueType();
        this.tier = tierFisrtLetterUpperCase(ranking.getTier());
        this.rank = ranking.getRank();
        this.leaguePoints = ranking.getLeaguePoints();
        this.wins = ranking.getWins();
        this.losses = ranking.getLosses();
        this.quantMatchs = ranking.getWins() +  ranking.getLosses();
        this.hotStreak = ranking.isHotStreak();
        this.winRate = Math.round((double) wins * 100 / quantMatchs);
    }

    public String tierFisrtLetterUpperCase(String tier) {

        char[] tierClear = new char[tier.toCharArray().length];

        for (int i = 0; i < tier.toCharArray().length; i++) {

            if (i == 0) {
                tierClear[i] = tier.toCharArray()[i];
            }else {
                tierClear[i] = tier.toLowerCase().toCharArray()[i];
            }

        }

        return new String(tierClear);
    }

}
