package com.your.summoner.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummonerProfileDto {

    String summonerName;

    String urlIcon;

    Integer level;

    List<RankingDto> rankingList;

    List<MainChampionDto> mainChampionList;
}
