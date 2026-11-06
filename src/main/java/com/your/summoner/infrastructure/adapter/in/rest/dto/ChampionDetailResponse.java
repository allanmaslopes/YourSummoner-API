package com.your.summoner.infrastructure.adapter.in.rest.dto;

import java.util.List;

public record ChampionDetailResponse(String id, long key, String name, String title, String lore, String blurb,
                                     List<String> tags, String partype, Info info, Passive passive,
                                     List<Spell> spells, List<Skin> skins, List<String> allyTips,
                                     List<String> enemyTips, String urlIcon) {

    public record Info(int attack, int defense, int magic, int difficulty) {
    }

    public record Passive(String name, String description) {
    }

    public record Spell(String id, String name, String description, String cooldown, String cost, String range) {
    }

    public record Skin(String id, int num, String name) {
    }
}
