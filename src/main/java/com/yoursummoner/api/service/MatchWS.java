package com.yoursummoner.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoursummoner.api.client.RiotgamesClient;
import com.yoursummoner.api.model.Match;
import com.yoursummoner.api.model.Summoner;
import com.yoursummoner.api.model.error.ApiError;
import com.yoursummoner.api.model.match.MatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MatchWS {

    @Autowired
    RiotgamesClient riotgamesClient;

    @Autowired
    SummonerWS summonerWS;

    @Autowired
    ChampionWS championWS;

    @Autowired
    ObjectMapper objectMapper;

    public List<String> getMatchId(String nick, MatchRequest matchRequest) throws ApiError {
        String[] matchList;
        String request;

        Summoner summoner = summonerWS.findSummoner(nick);

        try {
            request = riotgamesClient.findMatchList(summoner.getPuuid(), matchRequest);
            matchList = objectMapper.readValue(request, String[].class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(MatchWS.class, "findMatchId", "Error to list matchs id", ex.getLocalizedMessage());
        }

        if (matchList.length > 0) {
            return new ArrayList<>(Arrays.asList(matchList));
        } else {
            throw new ApiError(MatchWS.class, "findMatchId", "Match list is empty", HttpStatus.NO_CONTENT);
        }
    }

    public Match getMatch(String matchId) throws ApiError {
        Match match;

        try {
            match = objectMapper.readValue(riotgamesClient.findLastMatch(matchId), Match.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(MatchWS.class, "getMatch", "Error to create match", ex.getLocalizedMessage());
        }

        return match;
    }

}
