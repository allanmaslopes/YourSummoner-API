package com.your.summoner.domain.service;

import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.MasteredChampion;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Regras puras sobre maestrias: cruzamento com o catálogo, ordenação e filtro por lane. */
public final class MasteryRanking {

    private static final Comparator<MasteredChampion> BY_POINTS_DESC =
            Comparator.comparingLong((MasteredChampion m) -> m.mastery().championPoints()).reversed();

    private MasteryRanking() {
    }

    /**
     * Cruza as maestrias com o catálogo (pela chave numérica), descartando campeões que ainda não
     * estão no catálogo (ex.: lançados antes da atualização do Data Dragon). Ordena por pontos.
     */
    public static List<MasteredChampion> merge(List<ChampionMastery> masteries, Map<Long, Champion> catalog) {
        return masteries.stream()
                .map(m -> {
                    Champion c = catalog.get(m.championId());
                    return c == null ? null : new MasteredChampion(c, m, null);
                })
                .filter(Objects::nonNull)
                .sorted(BY_POINTS_DESC)
                .toList();
    }

    public static List<MasteredChampion> top(List<MasteredChampion> ranked, int limit) {
        return ranked.stream().sorted(BY_POINTS_DESC).limit(limit).toList();
    }

    /**
     * Mantém só os campeões jogados na lane (presentes no mapa de pick rate), preenchendo o pick
     * rate em percentual com três casas (0.03341 → 3.341). Ordena por maestria.
     */
    public static List<MasteredChampion> forLane(List<MasteredChampion> ranked, Map<Long, Double> lanePickRates) {
        return ranked.stream()
                .filter(m -> lanePickRates.containsKey(m.champion().key()))
                .map(m -> m.withPickRate(Math.round(lanePickRates.get(m.champion().key()) * 100_000) / 1000.0))
                .sorted(BY_POINTS_DESC)
                .toList();
    }
}
