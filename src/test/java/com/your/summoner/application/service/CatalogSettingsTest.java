package com.your.summoner.application.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogSettingsTest {

    @Test
    void localeIsReadOnEveryCall() {
        AtomicReference<String> locale = new AtomicReference<>("pt_BR");
        CatalogSettings settings = new CatalogSettings(locale::get, Duration.ofMinutes(15));

        assertEquals("pt_BR", settings.locale());

        locale.set("en_US");

        assertEquals("en_US", settings.locale());
    }
}
