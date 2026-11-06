package com.your.summoner.infrastructure.adapter.out.mongo;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("champions")
public record ChampionDocument(String patch, String locale, String championId, long key,
                               String name, String title, List<String> tags) {
}
