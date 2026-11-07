package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StaticDataRepository {
    List<Patch> findAllPatches();
    Optional<Patch> findPatch(String version);
    Optional<Patch> findActivePatch();
    void mergeVersions(List<String> versions);
    boolean tryLock(String version, String locale, Instant now, Duration staleAfter);
    void markReady(String version, String locale);
    void markFailed(String version, String locale);
    void activate(String version);
    void saveChampions(String patch, String locale, Collection<Champion> champions);
    void saveChampionDetail(String patch, String locale, ChampionDetail detail);
    void saveSkins(String patch, String locale, List<ChampionSkin> skins);
    void savePickRates(String patch, Lane lane, Map<Long, Double> pickRates);
    Map<Long, Champion> findChampions(String patch, String locale);
    Optional<ChampionDetail> findChampionDetail(String patch, String locale, String championId);
    List<ChampionSkin> findSkins(String patch, String locale, String championId);
    Optional<Map<Long, Double>> findPickRates(String patch, Lane lane);
}
