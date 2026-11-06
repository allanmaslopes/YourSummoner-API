package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.ChampionDetail;

public interface GetChampionDetailUseCase {

    /**
     * Aceita o id do Data Dragon ({@code MonkeyKing}) ou o nome exibido ({@code wukong}), sem diferenciar caixa.
     * {@code patch} nulo ou vazio vale o patch ativo.
     */
    ChampionDetail execute(String championName, String patch);
}
