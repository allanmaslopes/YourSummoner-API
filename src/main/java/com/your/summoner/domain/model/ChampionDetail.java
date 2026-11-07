package com.your.summoner.domain.model;

import java.util.List;

/** Ficha completa de um campeão (Data Dragon). */
public record ChampionDetail(String id, long key, String name, String title, String lore, String blurb,
                             List<String> tags, String partype, Info info, Passive passive,
                             List<Spell> spells, List<Skin> skins, List<String> allyTips,
                             List<String> enemyTips, String iconUrl) {

    public ChampionDetail {
        tags = tags == null ? List.of() : List.copyOf(tags);
        spells = spells == null ? List.of() : List.copyOf(spells);
        skins = skins == null ? List.of() : List.copyOf(skins);
        allyTips = allyTips == null ? List.of() : List.copyOf(allyTips);
        enemyTips = enemyTips == null ? List.of() : List.copyOf(enemyTips);
    }

    public record Info(int attack, int defense, int magic, int difficulty) {
    }

    public record Passive(String name, String description) {
    }

    public record Spell(String id, String name, String description, String cooldown, String cost, String range) {
    }

    public record Skin(String id, int num, String name) {
    }
}
