package com.your.summoner.api.service;

import com.your.summoner.api.client.SummonerClient;
import com.your.summoner.api.enumeration.RequestApiEnum;
import com.your.summoner.api.model.Ranking;
import com.your.summoner.api.model.Summoner;
import com.your.summoner.api.model.SummonerDetails;
import com.your.summoner.api.model.champion.ChampionDetail;
import com.your.summoner.api.model.dto.ChampionMasteryDto;
import com.your.summoner.api.model.dto.MainChampionDto;
import com.your.summoner.api.model.dto.RankingDto;
import com.your.summoner.api.model.dto.SummonerProfileDto;
import com.your.summoner.api.model.error.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SummonerService {

    @Autowired
    SummonerClient summonerClient;

    @Autowired
    ChampionService championService;

    public Summoner findSummoner(String nick, String tag) throws ApiError {
        Summoner summoner = summonerClient.findSummonerByNick(nick, tag);

        if (nick == null || nick.isBlank()) {
            throw new ApiError(ChampionService.class, "findSummoner", "Nickname is empty or blank", HttpStatus.BAD_REQUEST);
        }
        return summoner;
    }

    public SummonerProfileDto findSummonerProfile(String nick, String tag) throws ApiError {
        SummonerProfileDto summonerProfileDto;

        if (nick == null || nick.isBlank()) {
            throw new ApiError(ChampionService.class, "findSummoner", "Nickname is empty or blank", HttpStatus.BAD_REQUEST);
        }

        if (tag == null || tag.isBlank()) {
            throw new ApiError(ChampionService.class, "findSummoner", "Tag is empty or blank", HttpStatus.BAD_REQUEST);
        }

        Summoner summoner = summonerClient.findSummonerByNick(nick, tag);

        SummonerDetails summonerDetails =  summonerClient.findSummonerDetails(summoner.getPuuid());


        List<Ranking> rankingList = summonerClient.rankingBySummoner(summoner.getPuuid());

        List<ChampionMasteryDto> getChampionMasteryDto = championService.getChampionsByMastery(summoner.getGameName(), summoner.getTagLine());

        List<MainChampionDto> mainChampionList = mainChampionDtoBuilder(getChampionMasteryDto);

        summonerProfileDto = summonerProfileDtoConstruct(summoner, mainChampionList, rankingList, summonerDetails);

        return summonerProfileDto;
    }

    private List<MainChampionDto> mainChampionDtoBuilder(List<ChampionMasteryDto> getChampionMasteryDto) throws ApiError {

        List<MainChampionDto> returnList = new ArrayList<>();

        List<MainChampionDto> mainChampionList = getChampionMasteryDto.stream().map(MainChampionDto::new).collect(Collectors.toList());

        for (int i = 0; i < 3; i++) {
            MainChampionDto mainChampion = mainChampionList.get(i);
            returnList.add(mainChampionConstruct(mainChampion));
        }

        return returnList;
    }

    private MainChampionDto mainChampionConstruct(MainChampionDto mainChampion) throws ApiError {
        ChampionDetail championDetail = championService.getChampDetail(mainChampion.getId());

        if (championDetail != null) {
            mainChampion.setTags(championDetail.getTags());
        }

        mainChampion.setUrlIcon(urlIconChampionBuilder(mainChampion.getId()));

        return mainChampion;
    }

    private String urlIconChampionBuilder(String championName) {
        return String.format("%s/cdn/%s/img/champion/%s.png", RequestApiEnum.DDRAGON.getClientAmbiente(), StaticService.version, championName);
    }

    private SummonerProfileDto summonerProfileDtoConstruct(Summoner summoner, List<MainChampionDto> mainChampionList, List<Ranking> rankingList, SummonerDetails summonerDetails) {
        return new SummonerProfileDto(
                summoner.getGameName(),
                urlIconBuilder(summonerDetails.getProfileIconId()),
                summonerDetails.getSummonerLevel(),
                getRankingDto(rankingList),
                mainChampionList
        );
    }

    private List<RankingDto> getRankingDto(List<Ranking> rankingList) {
        return rankingList.stream().map(RankingDto::new).collect(Collectors.toList());
    }

    private String urlIconBuilder(Integer profileIconId) {
        return String.format("%s/cdn/%s/img/profileicon/%d.png", RequestApiEnum.DDRAGON.getClientAmbiente(), StaticService.version, profileIconId);
    }
}
