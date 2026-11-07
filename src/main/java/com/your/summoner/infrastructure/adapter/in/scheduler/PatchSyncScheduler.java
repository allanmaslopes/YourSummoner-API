package com.your.summoner.infrastructure.adapter.in.scheduler;

import com.your.summoner.application.port.in.SyncPatchesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "yoursummoner.patch-sync.enabled", havingValue = "true", matchIfMissing = true)
public class PatchSyncScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(PatchSyncScheduler.class);

    private final SyncPatchesUseCase sync;

    public PatchSyncScheduler(SyncPatchesUseCase sync) {
        this.sync = sync;
    }

    @Scheduled(cron = "${yoursummoner.patch-sync.cron}")
    public void run() {
        try {
            sync.syncLatest();
        } catch (RuntimeException e) {
            LOG.warn("Patch sync failed, will retry on the next run: {}", e.toString());
        }
    }
}
