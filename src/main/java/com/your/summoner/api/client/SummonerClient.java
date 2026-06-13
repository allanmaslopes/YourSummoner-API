package com.your.summoner.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.your.summoner.api.enumeration.RequestApiEnum;
import com.your.summoner.api.model.Ranking;
import com.your.summoner.api.model.Summoner;
import com.your.summoner.api.model.SummonerDetails;
import com.your.summoner.api.model.dto.SummonerProfileDto;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.StaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class SummonerClient {

    @Autowired
    private RESTClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    public Summoner findSummonerByNick(String nickName, String tag) throws ApiError {

        Summoner result;
        String uri = String.format("/riot/account/v1/accounts/by-riot-id/%s/%s", nickName, tag);

        try {
            String response = restClient.sendReceive(null, uri, RequestApiEnum.AMERICAS).getBody();
            result = objectMapper.readValue(response, Summoner.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(SummonerClient.class, "findSummonerByNick", "Falha ao converter request", ex.getLocalizedMessage());
        }


        return result;
    }

    public SummonerDetails findSummonerDetails(String encryptedPUUID) throws ApiError {

        SummonerDetails result;
        String uri = String.format("/lol/summoner/v4/summoners/by-puuid/%s", encryptedPUUID);

        try {
            String response = restClient.sendReceive(null, uri, RequestApiEnum.BR).getBody();
            result = objectMapper.readValue(response, SummonerDetails.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(SummonerClient.class, "findSummonerDetails", "Falha ao converter request", ex.getLocalizedMessage());
        }

        return result;
    }

    public String findProfileIcons() throws ApiError {
        ResponseEntity<String> result;

        String uri = String.format("/cdn/%s/data/pt_BR/profileicon.json", StaticService.version);

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.DDRAGON);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(SummonerClient.class, "findChampions", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }

    public List<Ranking> rankingBySummoner(String encryptedSummonerId) throws ApiError {
        List<Ranking> rankingList;

        String uri = String.format("/lol/league/v4/entries/by-puuid/%s", encryptedSummonerId);

        try {
            String result = restClient.sendReceive(null, uri, RequestApiEnum.BR).getBody();
            rankingList = Arrays.asList(objectMapper.readValue(result, Ranking[].class));
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(SummonerClient.class, "findChampions", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return rankingList;
    }

}
