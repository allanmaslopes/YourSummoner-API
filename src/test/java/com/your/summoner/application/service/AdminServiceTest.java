package com.your.summoner.application.service;

import com.your.summoner.application.port.in.SyncPatchesUseCase;
import com.your.summoner.application.port.out.AuditLog;
import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.AuditEntry;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AdminServiceTest {
    private final CredentialServiceTest.MemoryStore secrets = new CredentialServiceTest.MemoryStore();
    private final List<AuditEntry> entries = new ArrayList<>();
    private final List<Integer> requestedLimits = new ArrayList<>();
    private AppSettings stored = new AppSettings("pt_BR", Platform.BR1);
    private final SyncPatchesUseCase sync = mock(SyncPatchesUseCase.class);
    private AdminService service;

    @BeforeEach
    void setUp() {
        AuditLog audit = new AuditLog() {
            @Override
            public void record(String actor, String action, String target, String ip) {
                entries.add(new AuditEntry(actor, action, target, ip, Instant.now()));
            }

            @Override
            public List<AuditEntry> recent(int limit) {
                requestedLimits.add(limit);
                return entries;
            }
        };
        SettingsStore settings = new SettingsStore() {
            @Override
            public AppSettings get() {
                return stored;
            }

            @Override
            public void put(AppSettings value, String actor) {
                stored = value;
            }
        };
        service = new AdminService(new CredentialServiceTest.MemoryStore() {
            @Override
            public List<SecretMetadata> list() {
                return List.of(new SecretMetadata(SecretName.RIOT_API_KEY, "1234", Instant.now(), "admin"),
                        new SecretMetadata(SecretName.ADMIN_TOTP, "", Instant.now(), "admin"));
            }

            @Override
            public void put(SecretName name, String value, String actor) {
                secrets.put(name, value, actor);
            }
        }, settings, audit, sync, Runnable::run);
    }

    @Test
    void putStoresAndAuditsOnlyTheName() {
        service.put(SecretName.RIOT_API_KEY, "RGAPI-segredo-1234", "admin", "127.0.0.1");
        assertEquals("RGAPI-segredo-1234", secrets.values.get(SecretName.RIOT_API_KEY));
        assertEquals(1, entries.size());
        AuditEntry e = entries.get(0);
        assertEquals("secret.update", e.action());
        assertEquals("RIOT_API_KEY", e.target());
        assertFalse(e.toString().contains("segredo"));
    }

    @Test
    void rejectsInternalAndInvalidValues() {
        assertThrows(InvalidInputException.class, () -> service.put(SecretName.ADMIN_TOTP, "x", "admin", "ip"));
        assertThrows(InvalidInputException.class, () -> service.put(SecretName.RIOT_API_KEY, "", "admin", "ip"));
        assertThrows(InvalidInputException.class, () -> service.put(SecretName.RIOT_API_KEY, "has space", "admin", "ip"));
        assertThrows(InvalidInputException.class,
                () -> service.put(SecretName.RIOT_API_KEY, "a\r\nX-Evil: 1", "admin", "ip"));
        assertThrows(InvalidInputException.class, () -> service.put(SecretName.RIOT_API_KEY, "a".repeat(257), "admin", "ip"));
        assertEquals(0, entries.size());
    }

    @Test
    void listHidesInternalSecrets() {
        assertEquals(List.of(SecretName.RIOT_API_KEY), service.list().stream().map(SecretMetadata::name).toList());
    }

    @Test
    void settingsPutIsStoredAndAudited() {
        service.put(new AppSettings("en_US", Platform.NA1), "admin", "127.0.0.1");
        assertEquals(new AppSettings("en_US", Platform.NA1), service.get());
        assertEquals("settings.update", entries.get(0).action());
    }

    @Test
    void localeChangeTriggersSync() {
        AppSettings updated = new AppSettings("en_US", Platform.NA1);
        doAnswer(invocation -> {
            assertEquals(updated, service.get());
            return null;
        }).when(sync).syncLatest();

        service.put(updated, "admin", "127.0.0.1");

        verify(sync).syncLatest();
        assertEquals(updated, service.get());
    }

    @Test
    void sameLocaleDoesNotTriggerSync() {
        AppSettings updated = new AppSettings("pt_BR", Platform.NA1);

        service.put(updated, "admin", "127.0.0.1");

        assertEquals(updated, service.get());
        verifyNoInteractions(sync);
    }

    @Test
    void syncFailureIsSwallowed() {
        AppSettings updated = new AppSettings("en_US", Platform.NA1);
        doThrow(new IllegalStateException("Data Dragon unavailable")).when(sync).syncLatest();

        assertDoesNotThrow(() -> service.put(updated, "admin", "127.0.0.1"));

        verify(sync).syncLatest();
        assertEquals(updated, service.get());
        assertEquals("settings.update", entries.get(0).action());
    }

    @Test
    void recentClampsTheLimit() {
        service.recent(0);
        service.recent(100000);
        assertEquals(List.of(1, 200), requestedLimits);
    }
}
