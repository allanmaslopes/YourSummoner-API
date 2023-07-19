package com.yoursummoner.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoursummoner.api.client.RiotgamesClient;
import com.yoursummoner.api.model.Dto.ChampionDto;
import com.yoursummoner.api.model.Dto.ChampionMasteryDto;
import com.yoursummoner.api.model.Summoner;
import com.yoursummoner.api.model.champion.Champion;
import com.yoursummoner.api.model.champion.ChampionByMastery;
import com.yoursummoner.api.model.champion.ChampionDetail;
import com.yoursummoner.api.model.error.ApiError;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChampionWS {

    @Autowired
    RiotgamesClient riotgamesClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SummonerWS summonerWS;

    /**
     * @return {@code Map<String, Champion>}
     * @throws ApiError classe de exceção RiotGamesClient
     * @apiNote Requisição com campeões da página web <a href="http://ddragon.leagueoflegends.com/cdn/12.5.1/data/pt_BR/champion.json">http://ddragon.leagueoflegends.com/cdn/12.5.1/data/pt_BR/champion.json</a>
     */
    protected Map<String, Champion> mapChampions() throws ApiError {
        Map<String, Champion> mapChampions;
        String request;

        try {
            request = riotgamesClient.findChampions();
            //Mapeamento da resposta, dentro da chave "data" com o JSONObject vamos encontrar os campeões
            String campeoes = new JSONObject(request).getJSONObject("data").toString();
            //ObjectMapper + contrutor customizado vai retornar campões mapeados!
            mapChampions = objectMapper.readValue(campeoes, Champion.class).getchampionMap();
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "mapChampions", "Error to map champions", ex.getLocalizedMessage());
        }

        return mapChampions;
    }


    /**
     * @param championName nome do campeão
     * @return {@code ChampionDetail}
     * @throws ApiError classe de exceção RiotGamesClient
     * @apiNote Busca o campeão mais detalhado
     */
    public ChampionDetail getChampDetail(String championName) throws ApiError {
        ChampionDetail championDetail;
        String request;

        if (championName == null || championName.isBlank() || championName.isEmpty()) {
            throw new ApiError(ChampionWS.class, "getChampDetail", "Champion name is empty or blank", HttpStatus.BAD_REQUEST);
        }

        try {
            request = riotgamesClient.findChampion(championName);
            String championResponse = new JSONObject(request).getJSONObject("data").getJSONObject(championName).toString();
            championDetail = objectMapper.readValue(championResponse, ChampionDetail.class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "getChampDetail", "Error to find champion", ex.getLocalizedMessage());
        }

        return championDetail;
    }

    /**
     * @param encryptedSummonerId summonerId retornado da riot api
     * @return {@code List<ChampionByMastery>}
     * @throws ApiError classe de exceção RiotGamesClient
     * @apiNote Busca campeões por maestria
     */
    protected List<ChampionByMastery> getMasteryBySummoner(String encryptedSummonerId) throws ApiError {
        ChampionByMastery[] championMastery;
        String request;

        try {
            request = riotgamesClient.getChampionsByMastery(encryptedSummonerId);
            championMastery = objectMapper.readValue(request, ChampionByMastery[].class);
        } catch (ApiError ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "championMasteryBySummoner", "Error to list championsByMastery", ex.getLocalizedMessage());
        }

        return new ArrayList<>(Arrays.asList(championMastery));
    }

    /**
     * @param nick nick do invocador
     * @return {@code List<ChampionMasteryDto>}
     * @throws ApiError classe de exceção RiotGamesClient
     * @apiNote Chamada e construção do fluxo de lista de campões por maestria
     */
    public List<ChampionMasteryDto> getChampionsByMastery(String nick) throws ApiError {
        //Buscando invocador
        Summoner summoner = summonerWS.findSummoner(nick);

        //Recuperando maestrias por invocador
        List<ChampionByMastery> championsByMasteries = getMasteryBySummoner(summoner.getId());

        //Todos os champs
        List<ChampionDto> championDto = getCampeaoDtos();

        //Lista que vamos retornar Api
        return getChampionMasteryDto(championsByMasteries, championDto);
    }

    public List<ChampionDto> getCampeaoDtos() {
        return StaticWS.championsList.stream().map(ChampionDto::new).collect(Collectors.toList());
    }

    /**
     * @param masteryList retorno da api riotgames
     * @param dtoList     dto para parsear os dados
     * @return {@code List<ChampionMasteryDto>}
     * @throws ApiError classe de exceção RiotGamesClient
     * @apiNote Match entre campeoes com e sem maestria
     */
    public List<ChampionMasteryDto> getChampionMasteryDto(List<ChampionByMastery> masteryList, List<ChampionDto> dtoList) throws ApiError {

        List<ChampionMasteryDto> championsMasteryDtos = new ArrayList<>();
        try {
            //1 for para maestrias do jogador e segundo para Campeao
            for (ChampionByMastery championByMastery : masteryList) {
                String chmapionId = championByMastery.getChampionId().toString();

                if (!dtoList.isEmpty()) {
                    for (ChampionDto champDto : dtoList) {
                        if (champDto.getKey().equals(chmapionId)) {
                            ChampionMasteryDto championMasteryDto = new ChampionMasteryDto(championByMastery, champDto);
                            championsMasteryDtos.add(championMasteryDto);
                        }
                    }
                } else {
                    throw new ApiError(ChampionWS.class, "championByMastery", "Mastery list is empty", HttpStatus.NO_CONTENT);
                }
            }

            return championsMasteryDtos;

        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "championByMastery", "Error to merge champions with mastery ranks", ex.getLocalizedMessage());
        }
    }

    /**
     * @param nick nick do invocador
     * @param lane lane do jogador
     * @return {@code List<ChampionMasteryDto>}
     * @throws ApiError classe de exceção RiotGamesClient
     */
    public List<ChampionMasteryDto> getFilterSearchChampions(String nick, String lane) throws ApiError {
        List<ChampionMasteryDto> masteryDtos = listChampionsMostPlayed(nick, lane);
        List<ChampionMasteryDto> filterMasteryList = new ArrayList<>();

        try {

            for (ChampionMasteryDto championMasteryDto : masteryDtos) {

                if (!championMasteryDto.getChestWinned()) {
                    filterMasteryList.add(championMasteryDto);
                }

            }
        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "getCustomSearchChampions", "Error to filter champs by lane", ex.getLocalizedMessage());
        }

        //Ajustar validação
        if (filterMasteryList.isEmpty()) {
            throw new ApiError(ChampionWS.class, "getFilterSearchChampions", "Mastery list is empty, check de lane name", HttpStatus.BAD_REQUEST);
        }

        return filterMasteryList;
    }


    /**
     * @param lane escolhida pelo jogador
     * @return {@code Map<String,Double>}
     * @throws ApiError classe de exceção RiotGamesClient
     */
    public Map<String, Double> mapChampionsMostPlayed(String lane) throws ApiError {
        String request = riotgamesClient.getChampionsMostPlayed();

        String laneData = request.split("a.exports=")[1];

        laneData = laneData.replace("},function(){}]);", "");

        Map<String, Double> championsMap = new HashMap<>();

        JSONObject jsonObject = new JSONObject(laneData);

        try {
            Map<String, String> auxMap = objectMapper.readValue(jsonObject.get(lane).toString(), HashMap.class);

            for (Map.Entry<String, String> champion : auxMap.entrySet()) {
                championsMap.put(champion.getKey(), Double.parseDouble(champion.getValue()));
            }

            auxMap.clear();
        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "mapChampionsMostPlayed", "Error to map champions by lane", ex.getLocalizedMessage());
        }

        return championsMap;
    }

    /**
     * @param nick do invocador
     * @param lane lane escolhida pelo jogador
     * @return {@code List<ChampionMasteryDto>}
     * @throws ApiError classe de exceção RiotGamesClient
     */
    public List<ChampionMasteryDto> listChampionsMostPlayed(String nick, String lane) throws ApiError {
        List<ChampionMasteryDto> championMasteryDtoListAux = getChampionsByMastery(nick);
        Map<String, Double> mapChampions = mapChampionsMostPlayed(lane);
        List<ChampionMasteryDto> championMasteryDtoList = new ArrayList<>();

        try {
            for (Map.Entry<String, Double> map : mapChampions.entrySet()) {
                for (ChampionMasteryDto championMastery : championMasteryDtoListAux) {
                    if (championMastery.getKey().toString().equals(map.getKey())) {
                        championMastery.setPickRate(map.getValue() * 1000);
                        championMasteryDtoList.add(championMastery);
                    }
                }
            }

            championMasteryDtoListAux.clear();

        } catch (Exception ex) {
            throw new ApiError(ChampionWS.class, "listChampionsMostPlayed", "Error to list champions by lane", ex.getLocalizedMessage());
        }

        return championMasteryDtoList;
    }

}
