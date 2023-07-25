package com.your.summoner.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.your.summoner.api.enumeration.RequestApiEnum;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.model.match.MatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MatchClient {

    @Autowired
    private RESTClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    public String findMatchList(String puuId, MatchRequest matchRequest) throws ApiError {
        ResponseEntity<String> result;

        Map<String, String> uriComponents = objectMapper.convertValue(matchRequest, Map.class);

        String uri = String.format("/lol/match/v5/matches/by-puuid/%s/ids", puuId);

        try {
            result = restClient.sendReceive(uriComponents, uri, RequestApiEnum.AMERICAS);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(MatchClient.class, "findMatchList", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }

    public String findLastMatch(String matchId) throws ApiError {
        ResponseEntity<String> result;

        String uri = String.format("/lol/match/v5/matches/%s", matchId);

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.AMERICAS);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(MatchClient.class, "findLastMatch", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }
}
