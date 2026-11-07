package com.your.summoner.infrastructure.adapter.out.mongo;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("skins")
public record SkinDocument(String patch, String locale, String championId, String skinId,
                           int num, String name, String splashUrl, String loadingUrl) {
}
