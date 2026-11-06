package com.your.summoner.application.service;

import com.your.summoner.application.port.out.RiotCredentials;
import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.domain.model.SecretName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Vault value first, then the environment. Read on every call so a rotation applies at once. */
@Service
public class CredentialService implements RiotCredentials {
    private static final Logger LOG = LoggerFactory.getLogger(CredentialService.class);

    private final SecretStore secrets;
    private final EnvCredentialSettings env;

    public CredentialService(SecretStore secrets, EnvCredentialSettings env) {
        this.secrets = secrets;
        this.env = env;
    }

    @Override
    public Optional<String> riotApiKey() {
        Optional<String> fromVault = Optional.empty();
        try {
            fromVault = secrets.find(SecretName.RIOT_API_KEY).filter(v -> !v.isBlank());
        } catch (RuntimeException e) {
            // Vault down or undecryptable: keep serving with the environment key. No value in the log.
            LOG.warn("Secret vault unavailable, using environment credentials ({})", e.getClass().getSimpleName());
        }
        return fromVault.or(() -> Optional.ofNullable(env.riotApiToken()).filter(v -> !v.isBlank()));
    }
}
