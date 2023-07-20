package com.yoursummoner.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoursummoner.api.enumeration.RequestApiEnum;
import com.yoursummoner.api.model.error.ApiError;
import com.yoursummoner.api.model.match.MatchRequest;
import com.yoursummoner.api.service.StaticWS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RiotgamesClient {

    @Autowired
    private RESTClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    public String findChampions() throws ApiError {
        ResponseEntity<String> result;

        //Linguagem de retorno pt_BR
        String uri = String.format("/cdn/%s/data/pt_BR/champion.json", StaticWS.version);

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.DDRAGON);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(RiotgamesClient.class, "findChampions", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }

    public String findChampion(String championName) throws ApiError {
        ResponseEntity<String> result;

        String uri = String.format("/cdn/%s/data/pt_BR/champion/%s.json", StaticWS.version, championName);

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.DDRAGON);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(RiotgamesClient.class, "findChampion", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();

    }

    public ResponseEntity<String> findSummonerByNick(String nickName) throws ApiError {
        ResponseEntity<String> result;

        String uri = "/lol/summoner/v4/summoners/by-name/" + nickName;

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.BR);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(RiotgamesClient.class, "findSummonerByNick", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result;
    }

    public String getChampionsByMastery(String encryptedId) throws ApiError {
        ResponseEntity<String> result;

        String uri = "/lol/champion-mastery/v4/champion-masteries/by-summoner/" + encryptedId;

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.BR);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(RiotgamesClient.class, "getChampionsByMastery", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }

    public String findMatchList(String puuId, MatchRequest matchRequest) throws ApiError {
        ResponseEntity<String> result;

        Map<String, String> uriComponents = objectMapper.convertValue(matchRequest, Map.class);

        String uri = String.format("/lol/match/v5/matches/by-puuid/%s/ids", puuId);

        try {
            result = restClient.sendReceive(uriComponents, uri, RequestApiEnum.AMERICAS);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(RiotgamesClient.class, "findMatchList", "Falha ao efetuar request", ex.getLocalizedMessage());
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
            throw new ApiError(RiotgamesClient.class, "findLastMatch", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }


    public String getChampionsMostPlayed() throws ApiError {
        ResponseEntity<String> result;

        String uri = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-champion-statistics/global/default/rcp-fe-lol-champion-statistics.js";

        try {
            result = restClient.sendReceive(null, uri, null);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(RiotgamesClient.class, "getChampionsMostPlayed", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }


}
