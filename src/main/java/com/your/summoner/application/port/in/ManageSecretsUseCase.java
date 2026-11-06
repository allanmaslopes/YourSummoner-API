package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;

import java.util.List;

public interface ManageSecretsUseCase {
    List<SecretMetadata> list();

    void put(SecretName name, String value, String actor, String ip);
}
