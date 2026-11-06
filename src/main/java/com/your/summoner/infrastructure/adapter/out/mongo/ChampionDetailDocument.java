package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.domain.model.ChampionDetail;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("champion_details")
public record ChampionDetailDocument(String patch, String locale, String championId, ChampionDetail detail) {
}
