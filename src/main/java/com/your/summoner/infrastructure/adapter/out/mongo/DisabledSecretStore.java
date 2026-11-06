package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;

import java.util.List;
import java.util.Optional;

/** Used when no master key is configured: the vault is off and the environment is the only source. */
public class DisabledSecretStore implements SecretStore {
    @Override
    public Optional<String> find(SecretName name) {
        return Optional.empty();
    }

    @Override
    public void put(SecretName name, String value, String actor) {
        throw new IllegalStateException("Secret vault is disabled (no master key)");
    }

    @Override
    public List<SecretMetadata> list() {
        return List.of();
    }
}
