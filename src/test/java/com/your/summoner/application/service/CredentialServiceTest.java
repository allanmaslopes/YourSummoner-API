package com.your.summoner.application.service;

import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialServiceTest {

    static class MemoryStore implements SecretStore {
        final Map<SecretName, String> values = new EnumMap<>(SecretName.class);
        boolean failing;

        @Override
        public Optional<String> find(SecretName name) {
            if (failing) {
                throw new IllegalStateException("vault down");
            }
            return Optional.ofNullable(values.get(name));
        }

        @Override
        public void put(SecretName name, String value, String actor) {
            values.put(name, value);
        }

        @Override
        public List<SecretMetadata> list() {
            return List.of();
        }
    }

    @Test
    void vaultValueWinsOverEnv() {
        MemoryStore store = new MemoryStore();
        store.put(SecretName.RIOT_API_KEY, "vault-key", "admin");
        CredentialService service = new CredentialService(store, new EnvCredentialSettings("env-key"));
        assertEquals(Optional.of("vault-key"), service.riotApiKey());
    }

    @Test
    void fallsBackToEnvWhenVaultEmpty() {
        CredentialService service = new CredentialService(new MemoryStore(), new EnvCredentialSettings("env-key"));
        assertEquals(Optional.of("env-key"), service.riotApiKey());
        assertTrue(new CredentialService(new MemoryStore(), new EnvCredentialSettings("")).riotApiKey().isEmpty());
    }

    @Test
    void fallsBackToEnvWhenVaultFails() {
        MemoryStore store = new MemoryStore();
        store.failing = true;
        CredentialService service = new CredentialService(store, new EnvCredentialSettings("env-key"));
        assertEquals(Optional.of("env-key"), service.riotApiKey());
    }

    @Test
    void putIsVisibleOnNextCall() {
        MemoryStore store = new MemoryStore();
        CredentialService service = new CredentialService(store, new EnvCredentialSettings("env-key"));
        assertEquals(Optional.of("env-key"), service.riotApiKey());
        store.put(SecretName.RIOT_API_KEY, "rotated", "admin");
        assertEquals(Optional.of("rotated"), service.riotApiKey());
    }
}
