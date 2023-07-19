package com.yoursummoner.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoursummoner.api.client.RiotgamesClient;
import com.yoursummoner.api.model.Summoner;
import com.yoursummoner.api.model.error.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class SummonerWS {

    @Autowired
    RiotgamesClient riotgamesClient;

    @Autowired
    ObjectMapper objectMapper;

    public Summoner findSummoner(String nick) throws ApiError {
        Summoner summoner;

        if (nick == null || nick.isBlank() || nick.isEmpty()) {
            throw new ApiError(ChampionWS.class, "findSummoner", "Nickname is empty or blank", HttpStatus.BAD_REQUEST);
        }

        try {
            summoner = objectMapper.readValue(riotgamesClient.findSummonerByNick(nick).getBody(), Summoner.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(SummonerWS.class, "findSummoner", "Erro ao buscar invocador", ex.getLocalizedMessage());
        }

        return summoner;
    }

}
