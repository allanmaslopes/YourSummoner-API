package com.your.summoner.infrastructure.adapter.out.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Only ciphertext is stored; {@code last4} is the already-public mask of a manageable secret. */
@Document("secrets")
public record SecretDocument(@Id String name, String cipherText, String last4, Instant updatedAt, String updatedBy) {
    @Override
    public String toString() {
        return "SecretDocument[name=" + name + "]";
    }
}
