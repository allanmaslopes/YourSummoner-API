package com.your.summoner.domain.model;

import java.time.Instant;
import java.util.Objects;

public record SecretMetadata(SecretName name, String last4, Instant updatedAt, String updatedBy) {
    public SecretMetadata {
        Objects.requireNonNull(name);
    }

    public static String last4Of(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() < 8) {
            return "";
        }
        return value.substring(value.length() - 4);
    }
}
