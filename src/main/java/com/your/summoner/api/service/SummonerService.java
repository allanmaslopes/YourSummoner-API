package com.your.summoner.api.service;

import com.your.summoner.api.client.SummonerClient;
import com.your.summoner.api.enumeration.RequestApiEnum;
import com.your.summoner.api.model.Ranking;
import com.your.summoner.api.model.Summoner;
import com.your.summoner.api.model.dto.RankingDto;
import com.your.summoner.api.model.dto.SummonerProfileDto;
import com.your.summoner.api.model.error.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SummonerService {

    @Autowired
    SummonerClient summonerClient;

    public Summoner findSummoner(String nick) throws ApiError {
        Summoner summoner = summonerClient.findSummonerByNick(nick);

        if (nick == null || nick.isBlank() || nick.isEmpty()) {
            throw new ApiError(ChampionService.class, "findSummoner", "Nickname is empty or blank", HttpStatus.BAD_REQUEST);
        }
        return summoner;
    }

    public SummonerProfileDto findSummonerProfile(String nick) throws ApiError {
        SummonerProfileDto summonerProfileDto;

        if (nick == null || nick.isBlank() || nick.isEmpty()) {
            throw new ApiError(ChampionService.class, "findSummoner", "Nickname is empty or blank", HttpStatus.BAD_REQUEST);
        }

        Summoner summoner = summonerClient.findSummonerByNick(nick);

        List<Ranking> rankingList = summonerClient.rankingBySummoner(summoner.getId());

        summonerProfileDto = summonerProfileDtoConstruct(summoner, rankingList);

        return summonerProfileDto;
    }

    private SummonerProfileDto summonerProfileDtoConstruct(Summoner summoner, List<Ranking> rankingList) {
        return new SummonerProfileDto(
                summoner.getName(),
                urlIconBuilder(summoner.getProfileIconId()),
                summoner.getSummonerLevel(),
                getRankingDto(rankingList)
        );
    }

    private List<RankingDto> getRankingDto(List<Ranking> rankingList) {
        return rankingList.stream().map(RankingDto::new).collect(Collectors.toList());
    }

    private String urlIconBuilder(Integer profileIconId) {
        return String.format("%s/cdn/%s/img/profileicon/%d.png", RequestApiEnum.DDRAGON.getClientAmbiente(), StaticService.version, profileIconId);
    }
}
