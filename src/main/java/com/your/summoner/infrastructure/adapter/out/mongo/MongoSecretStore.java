package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.domain.model.SecretMetadata;
import com.your.summoner.domain.model.SecretName;
import com.your.summoner.infrastructure.security.AesGcmCipher;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class MongoSecretStore implements SecretStore {
    private final MongoTemplate mongo;
    private final AesGcmCipher cipher;

    public MongoSecretStore(MongoTemplate mongo, AesGcmCipher cipher) {
        this.mongo = mongo;
        this.cipher = cipher;
    }

    @Override
    public Optional<String> find(SecretName name) {
        return Optional.ofNullable(mongo.findById(name.name(), SecretDocument.class))
                .map(doc -> cipher.decrypt(doc.cipherText()));
    }

    @Override
    public void put(SecretName name, String value, String actor) {
        String last4 = name.manageable() ? SecretMetadata.last4Of(value) : "";
        mongo.save(new SecretDocument(name.name(), cipher.encrypt(value), last4, Instant.now(), actor));
    }

    @Override
    public List<SecretMetadata> list() {
        return mongo.findAll(SecretDocument.class).stream()
                .map(d -> new SecretMetadata(SecretName.valueOf(d.name()), d.last4(), d.updatedAt(), d.updatedBy()))
                .toList();
    }
}
