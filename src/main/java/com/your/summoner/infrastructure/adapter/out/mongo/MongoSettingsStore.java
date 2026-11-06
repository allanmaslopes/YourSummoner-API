package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.Platform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Persisted settings; the application.yml values are the initial ones. */
@Component
public class MongoSettingsStore implements SettingsStore {
    private static final String ID = "app";

    private final MongoTemplate mongo;
    private final AppSettings defaults;

    public MongoSettingsStore(MongoTemplate mongo,
                              @Value("${yoursummoner.locale:pt_BR}") String locale,
                              @Value("${yoursummoner.default-platform:BR1}") String defaultPlatform) {
        this.mongo = mongo;
        this.defaults = new AppSettings(locale, Platform.from(defaultPlatform));
    }

    @Override
    public AppSettings get() {
        SettingsDocument doc = mongo.findById(ID, SettingsDocument.class);
        return doc == null ? defaults : new AppSettings(doc.locale(), Platform.from(doc.defaultPlatform()));
    }

    @Override
    public void put(AppSettings settings, String actor) {
        mongo.save(new SettingsDocument(ID, settings.locale(), settings.defaultPlatform().name(), Instant.now(), actor));
    }
}
