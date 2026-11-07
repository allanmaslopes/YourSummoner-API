package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.domain.model.Platform;
import org.springframework.stereotype.Component;

/** {@code ?platform=} opcional; sem ele vale a plataforma padrão configurada (BR1). */
@Component
public class PlatformResolver {

    private final SettingsStore settingsStore;

    public PlatformResolver(SettingsStore settingsStore) {
        this.settingsStore = settingsStore;
    }

    public Platform resolve(String platform) {
        return platform == null || platform.isBlank() ? settingsStore.get().defaultPlatform() : Platform.from(platform);
    }
}
