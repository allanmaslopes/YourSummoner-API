package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Dados estáticos publicados pelo Data Dragon. */
public interface GameDataProvider {

    List<String> versions();

    Map<Long, Champion> champions(String patch, String locale);

    Optional<ChampionDetail> championDetail(String patch, String locale, String championId);

    String profileIconUrl(String patch, int profileIconId);

    String championIconUrl(String patch, String championId);

    String skinSplashUrl(String championId, int num);

    String skinLoadingUrl(String championId, int num);
}
