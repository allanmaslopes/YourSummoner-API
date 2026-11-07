package com.your.summoner.infrastructure.security;

import java.time.Instant;

public record AdminSession(String token, Instant expiresAt, String totpEnrollmentUri) {

    @Override
    public String toString() {
        return "AdminSession[expiresAt=" + expiresAt + "]";
    }
}
