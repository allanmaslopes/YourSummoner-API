package com.your.summoner.infrastructure.config;

import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.application.service.CatalogSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class CatalogConfig {

    @Bean
    CatalogSettings catalogSettings(SettingsStore settingsStore,
                                    @Value("${yoursummoner.patch-sync.lock-stale-after}") String lockStaleAfter) {
        return new CatalogSettings(() -> settingsStore.get().locale(), DurationStyle.detectAndParse(lockStaleAfter));
    }
}
