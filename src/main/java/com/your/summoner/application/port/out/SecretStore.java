package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;

import java.util.List;
import java.util.Optional;

/** Encrypted secret vault. Implementations must never expose values outside {@link #find}. */
public interface SecretStore {
    Optional<String> find(SecretName name);

    void put(SecretName name, String value, String actor);

    List<SecretMetadata> list();
}
