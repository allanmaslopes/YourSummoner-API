package com.your.summoner.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class CacheConfig {
    @Bean
    CaffeineCacheManager cacheManager() {
        var manager = new CaffeineCacheManager();
        registerCaches(manager);
        return manager;
    }

    private void registerCaches(CaffeineCacheManager manager) {
        register(manager, "riot-account", Duration.ofMinutes(30), 5000);
        register(manager, "riot-summoner", Duration.ofMinutes(5), 5000);
        register(manager, "riot-league", Duration.ofMinutes(2), 5000);
        register(manager, "riot-mastery", Duration.ofMinutes(10), 5000);
        register(manager, "riot-match-ids", Duration.ofMinutes(1), 2000);
        register(manager, "riot-match", Duration.ofHours(24), 2000);
        // Catálogo: dado imutável por patch, sem TTL e limitado por tamanho.
        registerUnbounded(manager, "catalog-champions", 20);
        registerUnbounded(manager, "catalog-detail", 5000);
        registerUnbounded(manager, "catalog-skins", 5000);
        register(manager, "catalog-active-patch", Duration.ofMinutes(1), 1);
    }

    private void registerUnbounded(CaffeineCacheManager manager, String name, long maximumSize) {
        manager.registerCustomCache(name, Caffeine.newBuilder().maximumSize(maximumSize).build());
    }

    private void register(CaffeineCacheManager manager, String name, Duration ttl, long maximumSize) {
        manager.registerCustomCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl).maximumSize(maximumSize).build());
    }
}
