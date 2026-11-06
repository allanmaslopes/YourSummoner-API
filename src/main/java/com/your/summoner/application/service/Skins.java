package com.your.summoner.application.service;

import com.your.summoner.application.port.out.GameDataProvider;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;

import java.util.List;

/** Skins por campeão a partir do detalhe, com as URLs do provedor. */
final class Skins {

    private Skins() {
    }

    static List<ChampionSkin> of(GameDataProvider gameData, ChampionDetail detail) {
        return detail.skins().stream()
                .map(skin -> new ChampionSkin(detail.id(), skin.id(), skin.num(), skin.name(),
                        gameData.skinSplashUrl(detail.id(), skin.num()),
                        gameData.skinLoadingUrl(detail.id(), skin.num())))
                .toList();
    }
}
