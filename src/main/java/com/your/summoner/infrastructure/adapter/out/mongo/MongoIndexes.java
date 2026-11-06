package com.your.summoner.infrastructure.adapter.out.mongo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexes implements InitializingBean {
    private final MongoTemplate mongoTemplate;

    public MongoIndexes(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        mongoTemplate.indexOps(PatchDocument.class)
                .ensureIndex(new Index().on("version", Direction.ASC).unique());
        mongoTemplate.indexOps(ChampionDocument.class)
                .ensureIndex(catalogKey().unique());
        mongoTemplate.indexOps(ChampionDetailDocument.class)
                .ensureIndex(catalogKey().unique());
        mongoTemplate.indexOps(SkinDocument.class)
                .ensureIndex(catalogKey().on("num", Direction.ASC).unique());
        mongoTemplate.indexOps(ChampionStatisticsDocument.class)
                .ensureIndex(new Index().on("patch", Direction.ASC).on("lane", Direction.ASC).unique());
    }

    private static Index catalogKey() {
        return new Index().on("patch", Direction.ASC).on("locale", Direction.ASC)
                .on("championId", Direction.ASC);
    }
}
