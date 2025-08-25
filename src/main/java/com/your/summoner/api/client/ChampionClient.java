package com.your.summoner.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.your.summoner.api.enumeration.RequestApiEnum;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.StaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ChampionClient {

    @Autowired
    private RESTClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    public String findChampions() throws ApiError {
        ResponseEntity<String> result;

        //Linguagem de retorno pt_BR
        String uri = String.format("/cdn/%s/data/pt_BR/champion.json", StaticService.version);

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.DDRAGON);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionClient.class, "findChampions", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }

    public String findChampion(String championName) throws ApiError {
        ResponseEntity<String> result;

        String uri = String.format("/cdn/%s/data/pt_BR/champion/%s.json", StaticService.version, championName);

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.DDRAGON);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionClient.class, "findChampion", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();

    }

    public String getChampionsByMastery(String encryptedId) throws ApiError {
        ResponseEntity<String> result;

        String uri = "/lol/champion-mastery/v4/champion-masteries/by-summoner/" + encryptedId;

        try {
            result = restClient.sendReceive(null, uri, RequestApiEnum.BR);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionClient.class, "getChampionsByMastery", "Falha ao efetuar request", ex.getLocalizedMessage());
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
            throw new ApiError(ChampionClient.class, "getChampionsMostPlayed", "Falha ao efetuar request", ex.getLocalizedMessage());
        }

        return result.getBody();
    }
}
