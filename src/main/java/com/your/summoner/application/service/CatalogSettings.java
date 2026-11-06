package com.your.summoner.application.service;

import java.time.Duration;

/** Único ponto de leitura do locale do catálogo (a Task 12 pode trocar a origem do valor). */
public record CatalogSettings(String locale, Duration lockStaleAfter) {
}
