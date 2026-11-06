package com.your.summoner.application.service;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/** Reads the current catalog locale whenever it is used. */
public record CatalogSettings(Supplier<String> localeSource, Duration lockStaleAfter) {
    public CatalogSettings {
        Objects.requireNonNull(localeSource, "localeSource");
        Objects.requireNonNull(lockStaleAfter, "lockStaleAfter");
    }

    public String locale() {
        return localeSource.get();
    }
}
