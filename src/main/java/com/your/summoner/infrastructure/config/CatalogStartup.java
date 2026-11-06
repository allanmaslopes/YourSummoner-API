package com.your.summoner.infrastructure.config;

import com.your.summoner.application.port.in.SyncPatchesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Sincroniza o catálogo na subida, sem bloquear a API. Substitui o StaticDataWarmup do Quarkus. */
@Component
@ConditionalOnProperty(name = "yoursummoner.patch-sync.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogStartup {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogStartup.class);

    private final SyncPatchesUseCase sync;

    public CatalogStartup(SyncPatchesUseCase sync) {
        this.sync = sync;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        Thread.ofVirtual().name("catalog-startup").start(this::syncQuietly);
    }

    public void syncQuietly() {
        try {
            sync.syncLatest();
        } catch (RuntimeException e) {
            LOG.warn("Catalog sync on startup failed, the job will retry: {}", e.toString());
        }
    }
}
