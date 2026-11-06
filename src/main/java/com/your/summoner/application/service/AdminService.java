package com.your.summoner.application.service;

import com.your.summoner.application.port.in.ManageSecretsUseCase;
import com.your.summoner.application.port.in.ManageSettingsUseCase;
import com.your.summoner.application.port.in.SyncPatchesUseCase;
import com.your.summoner.application.port.in.ViewAuditUseCase;
import com.your.summoner.application.port.out.AuditLog;
import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.AuditEntry;
import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class AdminService implements ManageSecretsUseCase, ManageSettingsUseCase, ViewAuditUseCase {
    private static final Logger LOG = LoggerFactory.getLogger(AdminService.class);
    private static final int MAX_SECRET_LENGTH = 256;

    private final SecretStore secrets;
    private final SettingsStore settings;
    private final AuditLog audit;
    private final SyncPatchesUseCase sync;
    private final Executor executor;

    public AdminService(SecretStore secrets, SettingsStore settings, AuditLog audit,
                        SyncPatchesUseCase sync, Executor adminSettingsExecutor) {
        this.secrets = secrets;
        this.settings = settings;
        this.audit = audit;
        this.sync = sync;
        // Resolved by parameter name: the context also has the scheduler executor.
        this.executor = adminSettingsExecutor;
    }

    @Override
    public List<SecretMetadata> list() {
        return secrets.list().stream().filter(m -> m.name().manageable()).toList();
    }

    @Override
    public void put(SecretName name, String value, String actor, String ip) {
        if (name == null || !name.manageable()) {
            throw new InvalidInputException("Unknown secret");
        }
        // Printable ASCII only: the value ends up in an HTTP header.
        if (value == null || value.isEmpty() || value.length() > MAX_SECRET_LENGTH
                || !value.chars().allMatch(c -> c >= 0x21 && c <= 0x7E)) {
            throw new InvalidInputException("Secret value is empty, too long or has invalid characters");
        }
        secrets.put(name, value, actor);
        audit.record(actor, "secret.update", name.name(), ip);
    }

    @Override
    public AppSettings get() {
        return settings.get();
    }

    @Override
    public void put(AppSettings newSettings, String actor, String ip) {
        AppSettings previous = settings.get();
        settings.put(newSettings, actor);
        audit.record(actor, "settings.update", "settings", ip);
        if (!previous.locale().equals(newSettings.locale())) {
            try {
                executor.execute(() -> {
                    try {
                        sync.syncLatest();
                    } catch (Exception e) {
                        LOG.warn("Catalog sync after locale change failed ({})", e.getClass().getSimpleName());
                    }
                });
            } catch (RuntimeException e) {
                LOG.warn("Catalog sync after locale change could not be scheduled ({})", e.getClass().getSimpleName());
            }
        }
    }

    @Override
    public List<AuditEntry> recent(int limit) {
        return audit.recent(Math.max(1, Math.min(limit, 200)));
    }
}
