package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Persisted settings; the application.yml values are the initial ones. */
@Component
public class MongoSettingsStore implements SettingsStore {
    private static final Logger LOG = LoggerFactory.getLogger(MongoSettingsStore.class);
    private static final String ID = "app";
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final MongoTemplate mongo;
    private final AppSettings defaults;
    private final Clock clock;
    private volatile CachedSettings cache;

    @Autowired
    public MongoSettingsStore(MongoTemplate mongo,
                              @Value("${yoursummoner.locale:pt_BR}") String locale,
                              @Value("${yoursummoner.default-platform:BR1}") String defaultPlatform) {
        this(mongo, locale, defaultPlatform, Clock.systemUTC());
    }

    public MongoSettingsStore(MongoTemplate mongo, String locale, String defaultPlatform, Clock clock) {
        this.mongo = mongo;
        this.defaults = new AppSettings(locale, Platform.from(defaultPlatform));
        this.clock = clock;
    }

    @Override
    public AppSettings get() {
        CachedSettings current = cache;
        if (current != null && clock.instant().isBefore(current.loadedAt().plus(CACHE_TTL))) {
            return current.settings();
        }
        synchronized (this) {
            current = cache;
            if (current != null && clock.instant().isBefore(current.loadedAt().plus(CACHE_TTL))) {
                return current.settings();
            }
            try {
                SettingsDocument doc = mongo.findById(ID, SettingsDocument.class);
                AppSettings value = doc == null ? defaults : new AppSettings(doc.locale(), Platform.from(doc.defaultPlatform()));
                cache = new CachedSettings(value, clock.instant());
                return value;
            } catch (DataAccessException e) {
                LOG.warn("Settings storage unavailable, using last known settings or defaults ({})",
                        e.getClass().getSimpleName());
                AppSettings fallback = current == null ? defaults : current.settings();
                cache = new CachedSettings(fallback, clock.instant());
                return fallback;
            }
        }
    }

    @Override
    public synchronized void put(AppSettings settings, String actor) {
        mongo.save(new SettingsDocument(ID, settings.locale(), settings.defaultPlatform().name(), clock.instant(), actor));
        // Force a refresh while retaining the successful write as an outage fallback.
        cache = new CachedSettings(settings, Instant.MIN);
    }

    private record CachedSettings(AppSettings settings, Instant loadedAt) {
    }
}
