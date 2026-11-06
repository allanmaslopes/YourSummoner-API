package com.your.summoner.domain.service;

import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionMastery;
import com.your.summoner.domain.model.MasteredChampion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MasteryRankingTest {

    private static final Map<Long, Champion> CATALOG = Map.of(
            266L, new Champion("Aatrox", 266, "Aatrox", "", List.of("Fighter")),
            103L, new Champion("Ahri", 103, "Ahri", "", List.of("Mage")),
            64L, new Champion("LeeSin", 64, "Lee Sin", "", List.of("Fighter")));

    private static final List<ChampionMastery> MASTERIES = List.of(
            new ChampionMastery(103, 10, 50_000, 0),
            new ChampionMastery(9999, 10, 900_000, 0),
            new ChampionMastery(64, 30, 250_000, 0),
            new ChampionMastery(266, 5, 10_000, 0));

    @Test
    void mergeDropsUnknownChampionsAndSortsByPoints() {
        List<MasteredChampion> ranked = MasteryRanking.merge(MASTERIES, CATALOG);

        assertEquals(List.of("LeeSin", "Ahri", "Aatrox"), ids(ranked));
        assertNull(ranked.getFirst().pickRate());
    }

    @Test
    void topLimitsTheList() {
        assertEquals(List.of("LeeSin", "Ahri"), ids(MasteryRanking.top(MasteryRanking.merge(MASTERIES, CATALOG), 2)));
        assertEquals(3, MasteryRanking.top(MasteryRanking.merge(MASTERIES, CATALOG), 10).size());
    }

    @Test
    void forLaneKeepsOnlyLaneChampionsAndConvertsPickRateToPercent() {
        var ranked = MasteryRanking.merge(MASTERIES, CATALOG);

        var lane = MasteryRanking.forLane(ranked, Map.of(266L, 0.05012, 103L, 0.00071));

        assertEquals(List.of("Ahri", "Aatrox"), ids(lane));
        assertEquals(0.071, lane.get(0).pickRate());
        assertEquals(5.012, lane.get(1).pickRate());
    }

    private static List<String> ids(List<MasteredChampion> list) {
        return list.stream().map(m -> m.champion().id()).toList();
    }
}
