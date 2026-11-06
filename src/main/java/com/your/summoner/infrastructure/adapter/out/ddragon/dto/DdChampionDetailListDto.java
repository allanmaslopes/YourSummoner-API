package com.your.summoner.infrastructure.adapter.out.ddragon.dto;

import java.util.List;
import java.util.Map;

/** {@code champion/{id}.json}: {@code {"data":{"Aatrox":{...}}}}. */
public record DdChampionDetailListDto(Map<String, Detail> data) {

    public record Detail(String id, String key, String name, String title, String lore, String blurb,
                         List<String> tags, String partype, Info info, Passive passive, List<Spell> spells,
                         List<Skin> skins, List<String> allytips, List<String> enemytips) {
    }

    public record Info(int attack, int defense, int magic, int difficulty) {
    }

    public record Passive(String name, String description) {
    }

    public record Spell(String id, String name, String description, String cooldownBurn, String costBurn,
                        String rangeBurn) {
    }

    /** {@code parentSkin} só vem preenchido nos chromas. */
    public record Skin(String id, int num, String name, Integer parentSkin) {
    }
}
