package com.your.summoner.api.service;

import com.your.summoner.api.client.SummonerClient;
import com.your.summoner.api.model.Summoner;
import com.your.summoner.api.model.error.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


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

}
