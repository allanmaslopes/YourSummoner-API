package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.StaticDataRepository;
import com.your.summoner.domain.model.Champion;
import com.your.summoner.domain.model.ChampionDetail;
import com.your.summoner.domain.model.ChampionSkin;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.Patch;
import com.your.summoner.domain.model.PatchStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bson.Document;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MongoStaticDataRepository implements StaticDataRepository {
    private final MongoTemplate mongoTemplate;
    private final CatalogDocumentMapper mapper;
    private final Cache championsCache;
    private final Cache detailCache;
    private final Cache skinsCache;
    private final Cache activePatchCache;

    public MongoStaticDataRepository(MongoTemplate mongoTemplate, CatalogDocumentMapper mapper, CacheManager caches) {
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
        this.championsCache = Objects.requireNonNull(caches.getCache("catalog-champions"));
        this.detailCache = Objects.requireNonNull(caches.getCache("catalog-detail"));
        this.skinsCache = Objects.requireNonNull(caches.getCache("catalog-skins"));
        this.activePatchCache = Objects.requireNonNull(caches.getCache("catalog-active-patch"));
    }

    @Override
    public synchronized List<Patch> findAllPatches() {
        return mongoTemplate.find(new Query().with(Sort.by("order")), PatchDocument.class).stream()
                .map(mapper::toPatch).toList();
    }

    @Override
    public synchronized Optional<Patch> findPatch(String version) {
        return Optional.ofNullable(mongoTemplate.findOne(patchQuery(version), PatchDocument.class))
                .map(mapper::toPatch);
    }

    @Override
    public synchronized Optional<Patch> findActivePatch() {
        return Optional.ofNullable(cached(activePatchCache, "active", () -> mongoTemplate.findOne(
                Query.query(Criteria.where("active").is(true)), PatchDocument.class), Objects::nonNull))
                .map(mapper::toPatch);
    }

    @Override
    public synchronized void mergeVersions(List<String> versions) {
        for (int order = 0; order < versions.size(); order++) {
            String version = versions.get(order);
            Update update = new Update().set("order", order)
                    .setOnInsert("version", version).setOnInsert("active", false)
                    .setOnInsert("statusByLocale", Map.of())
                    .setOnInsert("lockedAtByLocale", Map.of())
                    .setOnInsert("finishedAtByLocale", Map.of());
            mongoTemplate.upsert(patchQuery(version), update, PatchDocument.class);
        }
    }

    @Override
    public boolean tryLock(String version, String locale, Instant now, Duration staleAfter) {
        Criteria available = new Criteria().orOperator(
                Criteria.where("statusByLocale." + locale).ne(PatchStatus.INGESTING),
                Criteria.where("lockedAtByLocale." + locale).lt(now.minus(staleAfter)));
        Query query = Query.query(Criteria.where("version").is(version).andOperator(available));
        Update update = new Update().set("statusByLocale." + locale, PatchStatus.INGESTING)
                .set("lockedAtByLocale." + locale, now).unset("finishedAtByLocale." + locale);
        boolean locked = mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), PatchDocument.class) != null;
        activePatchCache.clear();
        return locked;
    }

    @Override
    public void markReady(String version, String locale) {
        finish(version, locale, PatchStatus.READY);
    }

    @Override
    public void markFailed(String version, String locale) {
        finish(version, locale, PatchStatus.FAILED);
    }

    @Override
    public synchronized void activate(String version) {
        if (!mongoTemplate.exists(patchQuery(version), PatchDocument.class)) {
            throw new IllegalArgumentException("Unknown patch: " + version);
        }
        AggregationUpdate update = Aggregation.newUpdate().set("active")
                .toValue(ComparisonOperators.Eq.valueOf("version").equalToValue(version));
        mongoTemplate.updateMulti(new Query(), update, PatchDocument.class);
        activePatchCache.clear();
    }

    @Override
    public void saveChampions(String patch, String locale, Collection<Champion> champions) {
        championsCache.evict(List.of(patch, locale));
        for (Champion champion : champions) {
            upsert(catalogQuery(patch, locale, champion.id()), mapper.toChampionDocument(patch, locale, champion));
        }
    }

    @Override
    public void saveChampionDetail(String patch, String locale, ChampionDetail detail) {
        detailCache.evict(List.of(patch, locale, detail.id()));
        upsert(catalogQuery(patch, locale, detail.id()), mapper.toDetailDocument(patch, locale, detail));
    }

    @Override
    public void saveSkins(String patch, String locale, List<ChampionSkin> skins) {
        skins.forEach(skin -> skinsCache.evict(List.of(patch, locale, skin.championId())));
        for (ChampionSkin skin : skins) {
            Query query = catalogQuery(patch, locale, skin.championId())
                    .addCriteria(Criteria.where("num").is(skin.num()));
            upsert(query, mapper.toSkinDocument(patch, locale, skin));
        }
    }

    @Override
    public void savePickRates(String patch, Lane lane, Map<Long, Double> pickRates) {
        upsert(statisticsQuery(patch, lane), mapper.toStatisticsDocument(patch, lane, pickRates));
    }

    @Override
    public Map<Long, Champion> findChampions(String patch, String locale) {
        return cached(championsCache, List.of(patch, locale), () -> {
            Map<Long, Champion> champions = new LinkedHashMap<>();
            for (ChampionDocument document : mongoTemplate.find(localeQuery(patch, locale), ChampionDocument.class)) {
                Champion champion = mapper.toChampion(document);
                champions.put(champion.key(), champion);
            }
            return Collections.unmodifiableMap(champions);
        }, found -> !found.isEmpty());
    }

    @Override
    public Optional<ChampionDetail> findChampionDetail(String patch, String locale, String championId) {
        return Optional.ofNullable(cached(detailCache, List.of(patch, locale, championId), () ->
                Optional.ofNullable(mongoTemplate.findOne(catalogQuery(patch, locale, championId),
                        ChampionDetailDocument.class)).map(mapper::toDetail).orElse(null), Objects::nonNull));
    }

    @Override
    public List<ChampionSkin> findSkins(String patch, String locale, String championId) {
        return cached(skinsCache, List.of(patch, locale, championId), () -> {
            Query query = catalogQuery(patch, locale, championId).with(Sort.by("num"));
            return mongoTemplate.find(query, SkinDocument.class).stream().map(mapper::toSkin).toList();
        }, found -> !found.isEmpty());
    }

    @Override
    public Optional<Map<Long, Double>> findPickRates(String patch, Lane lane) {
        return Optional.ofNullable(mongoTemplate.findOne(statisticsQuery(patch, lane), ChampionStatisticsDocument.class))
                .map(ChampionStatisticsDocument::pickRates);
    }

    /** L1 sem TTL para dado imutável; resultado vazio ou "não encontrado" nunca entra no cache. */
    @SuppressWarnings("unchecked")
    private static <T> T cached(Cache cache, Object key, Supplier<T> loader, Predicate<T> cacheable) {
        Cache.ValueWrapper hit = cache.get(key);
        if (hit != null) {
            return (T) hit.get();
        }
        T value = loader.get();
        if (cacheable.test(value)) {
            cache.put(key, value);
        }
        return value;
    }

    private void finish(String version, String locale, PatchStatus status) {
        Update update = new Update().set("statusByLocale." + locale, status)
                .set("finishedAtByLocale." + locale, Instant.now()).unset("lockedAtByLocale." + locale);
        mongoTemplate.updateFirst(patchQuery(version), update, PatchDocument.class);
        activePatchCache.clear();
    }

    private void upsert(Query query, Object document) {
        Document fields = new Document();
        mongoTemplate.getConverter().write(document, fields);
        Update update = new Update();
        fields.forEach(update::set);
        mongoTemplate.upsert(query, update, document.getClass());
    }

    private static Query patchQuery(String version) {
        return Query.query(Criteria.where("version").is(version));
    }

    private static Query localeQuery(String patch, String locale) {
        return Query.query(Criteria.where("patch").is(patch).and("locale").is(locale));
    }

    private static Query catalogQuery(String patch, String locale, String championId) {
        return localeQuery(patch, locale).addCriteria(Criteria.where("championId").is(championId));
    }

    private static Query statisticsQuery(String patch, Lane lane) {
        return Query.query(Criteria.where("patch").is(patch).and("lane").is(lane.name()));
    }
}
