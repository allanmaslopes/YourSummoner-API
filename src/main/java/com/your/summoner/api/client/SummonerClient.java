package com.your.summoner.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.your.summoner.api.enumeration.RequestApiEnum;
import com.your.summoner.api.model.Summoner;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.service.StaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SummonerClient {

    @Autowired
    private RESTClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    public Summoner findSummonerByNick(String nickName) throws ApiError {
        Summoner result;

        String uri = "/lol/summoner/v4/summoners/by-name/" + nickName;

        try {
            String response = restClient.sendReceive(null, uri, RequestApiEnum.BR).getBody();
            result = objectMapper.readValue(response, Summoner.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(SummonerClient.class, "findSummonerByNick", "Falha ao converter request", ex.getLocalizedMessage());
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

}
