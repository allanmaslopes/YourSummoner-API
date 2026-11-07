package com.your.summoner.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class PatchTest {
    @Test
    void statusForUnknownLocaleIsNotIngested() {
        Patch patch = new Patch("14.1.1", false, Map.of("pt_BR", PatchStatus.READY));
        assertEquals(PatchStatus.NOT_INGESTED, patch.statusFor("en_US"));
    }

    @Test
    void isReadyOnlyWhenLocaleReady() {
        Patch patch = new Patch("14.1.1", false, Map.of("pt_BR", PatchStatus.READY, "en_US", PatchStatus.INGESTING));
        assertTrue(patch.isReady("pt_BR"));
        assertFalse(patch.isReady("en_US"));
    }

    @Test
    void mapIsImmutable() {
        Patch patch = new Patch("14.1.1", false, Map.of("pt_BR", PatchStatus.READY));
        assertThrows(UnsupportedOperationException.class, () -> patch.statusByLocale().put("x", PatchStatus.READY));
        Map<String, PatchStatus> originalMap = new HashMap<>();
        originalMap.put("en_US", PatchStatus.READY);
        Patch patchWithOriginalMap = new Patch("14.1.1", false, originalMap);
        originalMap.put("x", PatchStatus.READY);
        assertEquals(Map.of("en_US", PatchStatus.READY), patchWithOriginalMap.statusByLocale());
    }

    @Test
    void nullMapBecomesEmpty() {
        Patch patch = new Patch("14.1.1", false, null);
        assertEquals(Map.of(), patch.statusByLocale());
    }

    @Test
    void isValidVersion() {
        assertTrue(Patch.isValidVersion("14.1.1"));
        assertTrue(Patch.isValidVersion("15.22.1"));
        assertFalse(Patch.isValidVersion("abc"));
        assertFalse(Patch.isValidVersion("lolpatch_3.7"));
        assertFalse(Patch.isValidVersion("14.1"));
        assertFalse(Patch.isValidVersion(null));
        assertFalse(Patch.isValidVersion("14.1.1 "));
    }
}
