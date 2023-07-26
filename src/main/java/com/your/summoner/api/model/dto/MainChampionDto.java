package com.your.summoner.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MainChampionDto {

    private String id;

    private Long key;

    private String championName;

    private Integer masteryPoints;

    private Integer masteryLevel;

    private Boolean chestWinned;

    private Double pickRate;

    private String urlIcon;

    private List<String> tags;

    public MainChampionDto(ChampionMasteryDto championMastery) {
        this.id = championMastery.getId();
        this.key = championMastery.getKey();
        this.championName = championMastery.getChampionName();
        this.masteryPoints = championMastery.getMasteryPoints();
        this.masteryLevel = championMastery.getMasteryLevel();
        this.chestWinned = championMastery.getChestWinned();
        this.pickRate = championMastery.getPickRate();
    }
}
