package com.yoursummoner.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoursummoner.api.client.RESTClient;
import com.yoursummoner.api.client.RiotgamesClient;
import com.yoursummoner.api.model.champion.Champion;
import com.yoursummoner.api.enumeration.RequestApiEnum;
import com.yoursummoner.api.model.error.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StaticWS {

    public static String version;

    protected static List<Champion> championsList;

    private static ChampionWS championWS;

    private static RESTClient restClient;

    public static List<Champion> getChampionsList() {
        return championsList;
    }

    private static ObjectMapper objectMapper;

    @Scheduled(cron = "* 45 5 * * *")
    public static void findChampionsList() {
        //Método que vai retirar do map e jogar dentro de um List
        //Por quê ? Porque é mais fácil de trabalhar com List
        List<Champion> allChampionsList = new ArrayList<>(0);
        log.info("Loading champions list...");
        try {
            //Obtendo valor dos campeões
            Map<String, Champion> championMap = championWS.mapChampions();

            //Validação com base no championMap
            if (!championMap.isEmpty()) {
                //Conversão dos valores do map para ArrayList
                allChampionsList = new ArrayList<>(championMap.values());
            } else {
                //Caso a lista esteja vazia não há muito o que ser feito, pode ser um erro no servidor...
                throw new ApiError(ChampionWS.class, "allChampionsList", "Map of champions isEmpty", HttpStatus.NO_CONTENT);
            }
        } catch (ApiError ex) {
            log.error("Error to call method allChampionsList -> {}, httpstatus -> {}", ex.getMessage(), ex.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error to call method allChampionsList -> {}", ex.getMessage());
        }

        championsList = allChampionsList;
        log.info("Champions list changed -> {}", allChampionsList.size());
    }

    @Scheduled(cron = "* 30 5 * * *")
    public static void findPatch() {
        ResponseEntity<String> result;
        String[] arrayVersion = new String[0];

        log.info("Loading new version...");
        try {
            result = restClient.sendReceive(null, "api/versions.json", RequestApiEnum.DDRAGON);
            arrayVersion = objectMapper.readValue(result.getBody(), String[].class);
        } catch (ApiError ex) {
            log.error("Error to call method loadFindVersion -> {}, httpstatus -> {}", ex.getMessage(), ex.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error to call method loadFindVersion -> {}", ex.getMessage());
        }
        version = arrayVersion[0];
        log.info("New version loaded -> {}", StaticWS.version);
    }

    @Autowired
    public void setRestClient(RESTClient restClient) {
        StaticWS.restClient = restClient;
    }

    @Autowired
    public void setChampionWS(ChampionWS championWS) {
        StaticWS.championWS = championWS;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        StaticWS.objectMapper = objectMapper;
    }
}
