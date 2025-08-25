package com.your.summoner.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.your.summoner.api.client.SummonerClient;
import com.your.summoner.api.model.error.ApiError;
import com.your.summoner.api.model.icon.ProfileIcon;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProfileIconService {

    @Autowired
    SummonerClient summonerClient;

    @Autowired
    ObjectMapper objectMapper;

    protected Map<String, ProfileIcon> mapProfileIcons() throws ApiError {
        Map<String, ProfileIcon> profileIconsMap;
        String request;

        try {
            request = summonerClient.findProfileIcons();
            //Mapeamento da resposta, dentro da chave "data" com o JSONObject vamos encontrar os campeões
            String profileIcons = new JSONObject(request).getJSONObject("data").toString();
            //ObjectMapper + contrutor customizado vai retornar campões mapeados!
            profileIconsMap = objectMapper.readValue(profileIcons, ProfileIcon.class).getProfileIconMap();
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionService.class, "mapProfileIcons", "Error to map profile icons", ex.getLocalizedMessage());
        }

        return profileIconsMap;
    }
}
