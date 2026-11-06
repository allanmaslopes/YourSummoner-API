package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.Lane;

import java.util.Map;

/** Estatísticas globais de pick rate por lane (Community Dragon). */
public interface ChampionStatisticsProvider {

    /** Chave numérica do campeão → fração de picks na lane (0.0334 = 3,34%). */
    Map<Long, Double> pickRates(Lane lane);
}
