package com.your.summoner.infrastructure.adapter.out.mongo;

import java.util.Map;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("champion_statistics")
public record ChampionStatisticsDocument(String patch, String lane, Map<Long, Double> pickRates) {
}
