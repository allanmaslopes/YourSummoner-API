package com.your.summoner.domain.model;

public enum SecretName {
    RIOT_API_KEY,
    /** Internal: 2FA seed of the admin user. Never listed nor writable through the admin API. */
    ADMIN_TOTP;

    public boolean manageable() {
        return this != ADMIN_TOTP;
    }
}
