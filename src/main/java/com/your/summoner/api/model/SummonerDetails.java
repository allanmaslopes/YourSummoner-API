package com.your.summoner.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SummonerDetails {

    private String puuid;

    private Integer profileIconId;

    private Long revisionDate;

    private Integer summonerLevel;
}
