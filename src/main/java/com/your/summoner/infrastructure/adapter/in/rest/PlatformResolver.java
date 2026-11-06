package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.domain.model.Platform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** {@code ?platform=} opcional; sem ele vale a plataforma padrão configurada (BR1). */
@Component
public class PlatformResolver {

    private final Platform defaultPlatform;

    public PlatformResolver(@Value("${yoursummoner.default-platform:BR1}") String defaultPlatform) {
        this.defaultPlatform = Platform.from(defaultPlatform);
    }

    public Platform resolve(String platform) {
        return platform == null || platform.isBlank() ? defaultPlatform : Platform.from(platform);
    }
}
