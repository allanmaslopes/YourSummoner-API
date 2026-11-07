package com.your.summoner.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpAndKeyTest {
    private static final String RFC_SEED =
            Base64.getEncoder().encodeToString("12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    @Test
    void rfc6238Vector() {
        assertEquals("287082", Totp.code(RFC_SEED, 59 / 30));
        assertEquals("081804", Totp.code(RFC_SEED, 1111111109L / 30));
    }

    @Test
    void windowIsPlusMinusOneStep() {
        long now = 1_000_000_000L;
        long step = now / 30;
        assertTrue(Totp.verify(RFC_SEED, Totp.code(RFC_SEED, step - 1), now));
        assertTrue(Totp.verify(RFC_SEED, Totp.code(RFC_SEED, step + 1), now));
        assertFalse(Totp.verify(RFC_SEED, Totp.code(RFC_SEED, step + 2), now));
        assertFalse(Totp.verify(RFC_SEED, null, now));
        assertFalse(Totp.verify(RFC_SEED, "12345", now));
    }

    @Test
    void uriCarriesBase32Seed() {
        String uri = Totp.uri(RFC_SEED, "YourSummoner", "admin");
        assertTrue(uri.startsWith("otpauth://totp/YourSummoner:admin?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ&"));
    }

    @Test
    void masterKeyValidation() {
        assertFalse(MasterKey.parse("").present());
        assertFalse(MasterKey.parse(null).present());
        String good = Base64.getEncoder().encodeToString(new byte[32]);
        assertTrue(MasterKey.parse(good).present());
        IllegalStateException notBase64 = assertThrows(IllegalStateException.class, () -> MasterKey.parse("%%%not-base64%%%"));
        assertFalse(notBase64.getMessage().contains("not-base64"));
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        IllegalStateException tooShort = assertThrows(IllegalStateException.class, () -> MasterKey.parse(shortKey));
        assertFalse(tooShort.getMessage().contains(shortKey));
        assertFalse(MasterKey.parse(good).toString().contains(good));
    }

    @Test
    void hkdfIsDeterministicAndSeparatedByInfo() {
        MasterKey key = MasterKey.parse(Base64.getEncoder().encodeToString(new byte[32]));
        assertArrayEquals(key.derive("a"), key.derive("a"));
        assertEquals(32, key.derive("a").length);
        assertFalse(java.util.Arrays.equals(key.derive("a"), key.derive("b")));
    }

    @Test
    void limiterBlocksAfterFiveFailuresAndReleasesAfterWindow() {
        Instant start = Instant.parse("2026-11-06T10:00:00Z");
        Clock[] clock = {Clock.fixed(start, ZoneOffset.UTC)};
        LoginRateLimiter limiter = new LoginRateLimiter(new Clock() {
            @Override
            public java.time.ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return clock[0].instant();
            }
        });
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire("1.1.1.1").isEmpty());
        }
        long retry = limiter.tryAcquire("1.1.1.1").orElseThrow();
        assertTrue(retry > 0 && retry <= 900 + 1);
        assertTrue(limiter.tryAcquire("2.2.2.2").isEmpty());
        limiter.release("2.2.2.2");
        assertTrue(limiter.tryAcquire("2.2.2.2").isEmpty());
        clock[0] = Clock.fixed(start.plusSeconds(15 * 60), ZoneOffset.UTC);
        assertTrue(limiter.tryAcquire("1.1.1.1").isEmpty());
    }
}
