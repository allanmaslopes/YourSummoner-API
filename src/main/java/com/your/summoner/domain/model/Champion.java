package com.your.summoner.domain.model;

import java.util.List;

/**
 * Campeão do catálogo estático (Data Dragon).
 *
 * @param id   identificador textual, ex.: {@code MonkeyKing}
 * @param key  identificador numérico usado pela Riot API, ex.: {@code 62}
 * @param name nome exibido, ex.: {@code Wukong}
 */
public record Champion(String id, long key, String name, String title, List<String> tags) {

    public Champion {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
