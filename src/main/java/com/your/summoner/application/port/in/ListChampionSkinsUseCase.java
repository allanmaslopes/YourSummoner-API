package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.ChampionSkin;

import java.util.List;

public interface ListChampionSkinsUseCase {

    /** Skins do campeão (id ou nome, sem diferenciar caixa). {@code patch} nulo vale o patch ativo. */
    List<ChampionSkin> execute(String championName, String patch);
}
