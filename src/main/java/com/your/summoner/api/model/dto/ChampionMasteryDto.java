package com.your.summoner.api.model.dto;

import com.your.summoner.api.model.champion.ChampionByMastery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChampionMasteryDto {

    private String id;

    private Long key;

    private String championName;

    private Integer masteryPoints;

    private Integer masteryLevel;

    private Boolean chestWinned;

    private Double pickRate;

    public ChampionMasteryDto(ChampionByMastery championByMastery, ChampionDto champion) {
        this.id = champion.getId();
        this.key = championByMastery.getChampionId();
        this.championName = champion.getName();
        this.masteryPoints = championByMastery.getChampionPoints();
        this.masteryLevel = championByMastery.getChampionLevel();
        this.chestWinned = championByMastery.getChestGranted();
        this.pickRate = 0.0;
    }
}
