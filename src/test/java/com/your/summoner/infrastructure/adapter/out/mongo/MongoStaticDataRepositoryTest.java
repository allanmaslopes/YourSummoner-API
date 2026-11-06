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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.mongodb.database=static-data-repository-test")
@ActiveProfiles("test")
class MongoStaticDataRepositoryTest {

    private static final String PATCH = "14.1.1";
    private static final String PT_BR = "pt_BR";
    private static final String EN_US = "en_US";
    private static final Instant NOW = Instant.parse("2026-11-06T12:00:00Z");
    private static final Duration STALE_AFTER = Duration.ofMinutes(15);

    @Autowired private StaticDataRepository repository;
    @Autowired private MongoTemplate mongoTemplate;

    @BeforeEach
    void clearCatalog() {
        for (String collection : List.of("patches", "champions", "champion_details", "skins", "champion_statistics")) {
            mongoTemplate.remove(new Query(), collection);
        }
    }

    @Test
    void mergeVersionsKeepsExistingStatus() {
        repository.mergeVersions(List.of(PATCH, "13.24.1"));
        repository.markReady(PATCH, PT_BR);
        repository.markFailed(PATCH, EN_US);
        assertThat(repository.tryLock("13.24.1", PT_BR, NOW, STALE_AFTER)).isTrue();
        repository.activate(PATCH);

        repository.mergeVersions(List.of("14.2.1", PATCH, "13.24.1"));
        repository.mergeVersions(List.of("14.2.1", PATCH, "13.24.1"));

        assertThat(repository.findAllPatches()).extracting(Patch::version)
                .containsExactly("14.2.1", PATCH, "13.24.1");
        Patch existing = repository.findPatch(PATCH).orElseThrow();
        assertThat(existing.active()).isTrue();
        assertThat(existing.statusFor(PT_BR)).isEqualTo(PatchStatus.READY);
        assertThat(existing.statusFor(EN_US)).isEqualTo(PatchStatus.FAILED);
        assertThat(repository.findPatch("13.24.1").orElseThrow().statusFor(PT_BR))
                .isEqualTo(PatchStatus.INGESTING);
        Patch added = repository.findPatch("14.2.1").orElseThrow();
        assertThat(added.active()).isFalse();
        assertThat(added.statusFor(PT_BR)).isEqualTo(PatchStatus.NOT_INGESTED);
        assertThat(added.statusFor(EN_US)).isEqualTo(PatchStatus.NOT_INGESTED);
        assertThat(mongoTemplate.count(new Query(), "patches")).isEqualTo(3);
    }

    @Test
    void tryLockIsExclusive() {
        repository.mergeVersions(List.of(PATCH));

        assertThat(repository.tryLock(PATCH, PT_BR, NOW, STALE_AFTER)).isTrue();
        assertThat(repository.tryLock(PATCH, PT_BR, NOW.plusSeconds(1), STALE_AFTER)).isFalse();
        assertThat(repository.findPatch(PATCH).orElseThrow().statusFor(PT_BR))
                .isEqualTo(PatchStatus.INGESTING);
    }

    @Test
    void tryLockTakesOverStaleLock() {
        repository.mergeVersions(List.of(PATCH));
        assertThat(repository.tryLock(PATCH, PT_BR, NOW.minus(Duration.ofMinutes(16)), STALE_AFTER))
                .isTrue();

        assertThat(repository.tryLock(PATCH, PT_BR, NOW, STALE_AFTER)).isTrue();
        assertThat(repository.tryLock(PATCH, PT_BR, NOW.plusSeconds(1), STALE_AFTER)).isFalse();
        assertThat(repository.findPatch(PATCH).orElseThrow().statusFor(PT_BR))
                .isEqualTo(PatchStatus.INGESTING);
    }

    @Test
    void activateLeavesSingleActive() {
        repository.mergeVersions(List.of("14.2.1", PATCH, "13.24.1"));
        repository.markReady("13.24.1", PT_BR);
        repository.markReady(PATCH, PT_BR);
        repository.markReady("14.2.1", PT_BR);
        repository.activate("13.24.1");
        repository.activate(PATCH);

        repository.activate("14.2.1");
        repository.activate("14.2.1");

        assertThat(mongoTemplate.count(Query.query(Criteria.where("active").is(true)), PatchDocument.class))
                .isEqualTo(1);
        assertThat(repository.findActivePatch()).get().extracting(Patch::version).isEqualTo("14.2.1");
        assertThat(repository.findPatch(PATCH).orElseThrow().active()).isFalse();
        assertThat(repository.findPatch("13.24.1").orElseThrow().active()).isFalse();
    }

    @Test
    void findActivePrefersNewestWhenTwoAreActive() {
        repository.mergeVersions(List.of("14.2.1", PATCH));
        mongoTemplate.updateMulti(new Query(),
                new org.springframework.data.mongodb.core.query.Update().set("active", true), PatchDocument.class);
        repository.markReady(PATCH, PT_BR); // clears the active-patch cache after the direct database update

        assertThat(repository.findActivePatch()).get().extracting(Patch::version).isEqualTo("14.2.1");
    }

    @Test
    void saveIsIdempotent() {
        Champion champion = champion("Ahri", "the Nine-Tailed Fox");
        ChampionDetail detail = detail("Ahri", "the Nine-Tailed Fox");
        ChampionSkin skin = skin(1, "Dynasty Ahri");
        Map<Long, Double> pickRates = Map.of(103L, 0.12, 62L, 0.04);

        for (int attempt = 0; attempt < 2; attempt++) {
            repository.saveChampions(PATCH, EN_US, List.of(champion));
            repository.saveChampionDetail(PATCH, EN_US, detail);
            repository.saveSkins(PATCH, EN_US, List.of(skin));
            repository.savePickRates(PATCH, Lane.MIDDLE, pickRates);
        }

        assertThat(mongoTemplate.count(new Query(), "champions")).isEqualTo(1);
        assertThat(mongoTemplate.count(new Query(), "champion_details")).isEqualTo(1);
        assertThat(mongoTemplate.count(new Query(), "skins")).isEqualTo(1);
        assertThat(mongoTemplate.count(new Query(), "champion_statistics")).isEqualTo(1);
        assertThat(repository.findChampions(PATCH, EN_US)).containsExactlyEntriesOf(Map.of(103L, champion));
        assertThat(repository.findChampionDetail(PATCH, EN_US, "Ahri")).contains(detail);
        assertThat(repository.findSkins(PATCH, EN_US, "Ahri")).containsExactly(skin);
        assertThat(repository.findPickRates(PATCH, Lane.MIDDLE)).contains(pickRates);
        assertThat(repository.findPickRates(PATCH, Lane.TOP)).isEmpty();
        assertThat(repository.findChampions("13.24.1", EN_US)).isEmpty();
        assertThat(repository.findChampionDetail("13.24.1", EN_US, "Ahri")).isEmpty();
        assertThat(repository.findSkins("13.24.1", EN_US, "Ahri")).isEmpty();
        assertThat(repository.findPickRates("13.24.1", Lane.MIDDLE)).isEmpty();
    }

    @Test
    void skinsOrderedByNum() {
        ChampionSkin defaultSkin = skin(0, "default");
        ChampionSkin first = skin(1, "Dynasty Ahri");
        ChampionSkin later = skin(12, "Elderwood Ahri");
        repository.saveSkins(PATCH, EN_US, List.of(later, defaultSkin, first));

        assertThat(repository.findSkins(PATCH, EN_US, "Ahri"))
                .containsExactly(defaultSkin, first, later);
        assertThat(repository.findSkins(PATCH, EN_US, "Wukong")).isEmpty();
    }

    @Test
    void localesAreIsolated() {
        repository.mergeVersions(List.of(PATCH));
        assertThat(repository.tryLock(PATCH, PT_BR, NOW, STALE_AFTER)).isTrue();
        assertThat(repository.tryLock(PATCH, EN_US, NOW, STALE_AFTER)).isTrue();
        repository.markReady(PATCH, PT_BR);
        repository.markFailed(PATCH, EN_US);
        Champion portuguese = champion("Ahri", "a Raposa de Nove Caudas");
        Champion english = champion("Ahri", "the Nine-Tailed Fox");
        ChampionDetail portugueseDetail = detail("Ahri", "a Raposa de Nove Caudas");
        ChampionDetail englishDetail = detail("Ahri", "the Nine-Tailed Fox");
        ChampionSkin portugueseSkin = skin(1, "Ahri Dinastia");
        ChampionSkin englishSkin = skin(1, "Dynasty Ahri");

        repository.saveChampions(PATCH, PT_BR, List.of(portuguese));
        repository.saveChampionDetail(PATCH, PT_BR, portugueseDetail);
        repository.saveSkins(PATCH, PT_BR, List.of(portugueseSkin));
        repository.saveChampions(PATCH, EN_US, List.of(english));
        repository.saveChampionDetail(PATCH, EN_US, englishDetail);
        repository.saveSkins(PATCH, EN_US, List.of(englishSkin));

        assertThat(repository.findChampions(PATCH, PT_BR)).containsExactlyEntriesOf(Map.of(103L, portuguese));
        assertThat(repository.findChampions(PATCH, EN_US)).containsExactlyEntriesOf(Map.of(103L, english));
        assertThat(repository.findChampionDetail(PATCH, PT_BR, "Ahri")).contains(portugueseDetail);
        assertThat(repository.findChampionDetail(PATCH, EN_US, "Ahri")).contains(englishDetail);
        assertThat(repository.findSkins(PATCH, PT_BR, "Ahri")).containsExactly(portugueseSkin);
        assertThat(repository.findSkins(PATCH, EN_US, "Ahri")).containsExactly(englishSkin);
        Patch patch = repository.findPatch(PATCH).orElseThrow();
        assertThat(patch.statusFor(PT_BR)).isEqualTo(PatchStatus.READY);
        assertThat(patch.statusFor(EN_US)).isEqualTo(PatchStatus.FAILED);
        assertThat(repository.findChampions(PATCH, "fr_FR")).isEmpty();
        assertThat(repository.findChampionDetail(PATCH, "fr_FR", "Ahri")).isEmpty();
        assertThat(repository.findSkins(PATCH, "fr_FR", "Ahri")).isEmpty();
    }

    private static Champion champion(String name, String title) {
        return new Champion("Ahri", 103L, name, title, List.of("Mage", "Assassin"));
    }

    private static ChampionDetail detail(String name, String title) {
        return new ChampionDetail("Ahri", 103L, name, title, "Ahri's lore", "Ahri's blurb",
                List.of("Mage", "Assassin"), "Mana", new ChampionDetail.Info(3, 4, 8, 5),
                new ChampionDetail.Passive("Essence Theft", "Heal after defeating enemies"),
                List.of(new ChampionDetail.Spell("AhriOrbofDeception", "Orb of Deception",
                        "Throws an orb", "7", "55", "970")),
                List.of(new ChampionDetail.Skin("103001", 1, "Dynasty Ahri")),
                List.of("Use Charm before your spells"), List.of("Dodge Charm"),
                "https://ddragon.leagueoflegends.com/cdn/14.1.1/img/champion/Ahri.png");
    }

    private static ChampionSkin skin(int num, String name) {
        return new ChampionSkin("Ahri", "103%03d".formatted(num), num, name,
                "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Ahri_" + num + ".jpg",
                "https://ddragon.leagueoflegends.com/cdn/img/champion/loading/Ahri_" + num + ".jpg");
    }
}
