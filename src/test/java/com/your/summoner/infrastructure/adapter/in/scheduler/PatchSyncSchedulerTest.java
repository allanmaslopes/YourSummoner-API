package com.your.summoner.infrastructure.adapter.in.scheduler;

import com.your.summoner.application.port.in.SyncPatchesUseCase;
import com.your.summoner.application.service.CatalogSettings;
import com.your.summoner.infrastructure.config.CatalogConfig;
import com.your.summoner.infrastructure.config.CatalogStartup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class PatchSyncSchedulerTest {

    private final SyncPatchesUseCase sync = mock(SyncPatchesUseCase.class);
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(SyncPatchesUseCase.class, () -> sync)
            .withUserConfiguration(PatchSyncScheduler.class, CatalogStartup.class);

    @Test
    void runDelegatesAndSwallowsFailure() {
        doThrow(new IllegalStateException("down")).when(sync).syncLatest();

        new PatchSyncScheduler(sync).run();

        verify(sync).syncLatest();
    }

    @Test
    void disabledByProperty() {
        runner.withPropertyValues("yoursummoner.patch-sync.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(PatchSyncScheduler.class);
            assertThat(context).doesNotHaveBean(CatalogStartup.class);
        });
    }

    @Test
    void enabledByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(PatchSyncScheduler.class);
            assertThat(context).hasSingleBean(CatalogStartup.class);
        });
    }

    @Test
    void startupSyncsOnVirtualThreadAndFailureOnlyWarns() {
        doThrow(new IllegalStateException("down")).when(sync).syncLatest();
        CatalogStartup startup = new CatalogStartup(sync);

        startup.syncQuietly();
        startup.onReady();

        verify(sync, timeout(2000).times(2)).syncLatest();
    }

    @Test
    void settingsComeFromProperties() {
        new ApplicationContextRunner().withUserConfiguration(CatalogConfig.class)
                .withPropertyValues("yoursummoner.locale=en_US", "yoursummoner.patch-sync.lock-stale-after=15m")
                .run(context -> assertThat(context.getBean(CatalogSettings.class))
                        .isEqualTo(new CatalogSettings("en_US", Duration.ofMinutes(15))));
    }
}
