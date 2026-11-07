package com.your.summoner.domain.model;

import com.your.summoner.domain.exception.InvalidInputException;

import java.util.Locale;

/** Rotas usadas nas estatísticas de pick rate do Community Dragon. */
public enum Lane {
    TOP,
    JUNGLE,
    MIDDLE,
    BOTTOM,
    SUPPORT;

    /** Aceita o nome da lane e apelidos comuns (mid, adc, sup...). */
    public static Lane from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException("Lane is empty or blank");
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "TOP" -> TOP;
            case "JUNGLE", "JG", "JUNGLER" -> JUNGLE;
            case "MIDDLE", "MID" -> MIDDLE;
            case "BOTTOM", "BOT", "ADC", "CARRY" -> BOTTOM;
            case "SUPPORT", "SUP", "UTILITY" -> SUPPORT;
            default -> throw new InvalidInputException(
                    "Unknown lane '%s'. Valid values: TOP, JUNGLE, MIDDLE, BOTTOM, SUPPORT".formatted(value));
        };
    }
}
