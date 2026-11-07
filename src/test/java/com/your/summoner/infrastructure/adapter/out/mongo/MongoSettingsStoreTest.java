package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoSettingsStoreTest {

    private static final Instant START = Instant.parse("2026-11-06T12:00:00Z");
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final Clock clock = mock(Clock.class);
    private final AtomicReference<Instant> now = new AtomicReference<>(START);
    private MongoSettingsStore store;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenAnswer(invocation -> now.get());
        store = new MongoSettingsStore(mongo, "pt_BR", "BR1", clock);
    }

    @Test
    void cachesFor30Seconds() {
        when(mongo.findById("app", SettingsDocument.class)).thenReturn(
                document("pt_BR", "BR1"), document("en_US", "NA1"));

        assertEquals(new AppSettings("pt_BR", Platform.BR1), store.get());

        now.set(START.plusSeconds(29));
        assertEquals(new AppSettings("pt_BR", Platform.BR1), store.get());
        verify(mongo).findById("app", SettingsDocument.class);

        now.set(START.plusSeconds(30));
        assertEquals(new AppSettings("en_US", Platform.NA1), store.get());
        verify(mongo, times(2)).findById("app", SettingsDocument.class);
    }

    @Test
    void putInvalidatesCache() {
        when(mongo.findById("app", SettingsDocument.class)).thenReturn(
                document("pt_BR", "BR1"), document("en_US", "EUW1"));
        assertEquals(new AppSettings("pt_BR", Platform.BR1), store.get());

        store.put(new AppSettings("en_US", Platform.EUW1), "admin");

        verify(mongo).save(new SettingsDocument("app", "en_US", "EUW1", START, "admin"));
        assertEquals(new AppSettings("en_US", Platform.EUW1), store.get());
        verify(mongo, times(2)).findById("app", SettingsDocument.class);
    }

    @Test
    void mongoFailureReturnsLastKnownValue() {
        when(mongo.findById("app", SettingsDocument.class))
                .thenReturn(document("en_US", "NA1"))
                .thenThrow(new DataAccessResourceFailureException("Mongo unavailable"));
        assertEquals(new AppSettings("en_US", Platform.NA1), store.get());

        now.set(START.plusSeconds(30));

        assertEquals(new AppSettings("en_US", Platform.NA1), store.get());
        verify(mongo, times(2)).findById("app", SettingsDocument.class);
    }

    @Test
    void mongoFailureWithoutCacheReturnsDefaults() {
        store = new MongoSettingsStore(mongo, "en_US", "EUW1", clock);
        when(mongo.findById("app", SettingsDocument.class))
                .thenThrow(new DataAccessResourceFailureException("Mongo unavailable"));

        assertEquals(new AppSettings("en_US", Platform.EUW1), store.get());
    }

    @Test
    void mongoFailureCachesDefaultsFor30Seconds() {
        when(mongo.findById("app", SettingsDocument.class))
                .thenThrow(new DataAccessResourceFailureException("Mongo unavailable"));

        assertEquals(new AppSettings("pt_BR", Platform.BR1), store.get());

        now.set(START.plusSeconds(29));
        assertEquals(new AppSettings("pt_BR", Platform.BR1), store.get());
        verify(mongo).findById("app", SettingsDocument.class);

        now.set(START.plusSeconds(30));
        assertEquals(new AppSettings("pt_BR", Platform.BR1), store.get());
        verify(mongo, times(2)).findById("app", SettingsDocument.class);
    }

    private static SettingsDocument document(String locale, String platform) {
        return new SettingsDocument("app", locale, platform, START, "admin");
    }
}
