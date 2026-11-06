package com.your.summoner.domain.model;

import com.your.summoner.domain.exception.InvalidInputException;

import java.util.Locale;
import java.util.Set;

/** Filtros da match-v5 ({@code /matches/by-puuid/{puuid}/ids}). Todos opcionais. */
public record MatchQuery(Long startTime, Long endTime, Integer queue, String type, Integer start, Integer count) {

    private static final Set<String> TYPES = Set.of("ranked", "normal", "tourney", "tutorial");

    public MatchQuery {
        if (count != null && (count < 0 || count > 100)) {
            throw new InvalidInputException("quantity must be between 0 and 100");
        }
        if (start != null && start < 0) {
            throw new InvalidInputException("start must be >= 0");
        }
        if (startTime != null && endTime != null && startTime > endTime) {
            throw new InvalidInputException("startTime must be before endTime");
        }
        if (type != null) {
            type = type.trim().toLowerCase(Locale.ROOT);
            if (!TYPES.contains(type)) {
                throw new InvalidInputException("type must be one of ranked, normal, tourney, tutorial");
            }
        }
    }

    public static MatchQuery empty() {
        return new MatchQuery(null, null, null, null, null, null);
    }
}
