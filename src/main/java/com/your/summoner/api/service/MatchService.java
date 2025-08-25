package com.your.summoner.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.your.summoner.api.client.MatchClient;
import com.your.summoner.api.model.Match;
import com.your.summoner.api.model.Summoner;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.model.match.MatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MatchService {

    @Autowired
    MatchClient matchClient;

    @Autowired
    SummonerService summonerService;

    @Autowired
    ObjectMapper objectMapper;

    public List<String> getMatchId(String nick, MatchRequest matchRequest) throws ApiError {
        String[] matchList;
        String request;

        Summoner summoner = summonerService.findSummoner(nick);

        try {
            request = matchClient.findMatchList(summoner.getPuuid(), matchRequest);
            matchList = objectMapper.readValue(request, String[].class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(MatchService.class, "findMatchId", "Error to list matchs id", ex.getLocalizedMessage());
        }

        if (matchList.length > 0) {
            return new ArrayList<>(Arrays.asList(matchList));
        } else {
            throw new ApiError(MatchService.class, "findMatchId", "Match list is empty", HttpStatus.NO_CONTENT);
        }
    }

    public Match getMatch(String matchId) throws ApiError {
        Match match;

        try {
            match = objectMapper.readValue(matchClient.findLastMatch(matchId), Match.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(MatchService.class, "getMatch", "Error to create match", ex.getLocalizedMessage());
        }

        return match;
    }

}
