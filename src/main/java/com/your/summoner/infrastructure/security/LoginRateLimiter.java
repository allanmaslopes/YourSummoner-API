package com.your.summoner.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.OptionalLong;

/** 5 failed logins per 15 minutes per client address, in memory. */
public class LoginRateLimiter {
    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Window(int failures, Instant start) {
    }

    private final Clock clock;
    // shortcut: bounded and per instance; a flood from >10k addresses can evict entries. Use a shared store if the API scales out.
    private final Cache<String, Window> failures =
            Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(WINDOW).build();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Seconds until the address may try again, or empty when it is not blocked. */
    public OptionalLong retryAfterSeconds(String ip) {
        Window w = failures.getIfPresent(ip);
        Instant now = clock.instant();
        if (w == null || w.failures() < MAX_FAILURES || !now.isBefore(w.start().plus(WINDOW))) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Math.max(1, Duration.between(now, w.start().plus(WINDOW)).toSeconds() + 1));
    }

    public void recordFailure(String ip) {
        Instant now = clock.instant();
        failures.asMap().compute(ip, (k, w) -> w == null || !now.isBefore(w.start().plus(WINDOW))
                ? new Window(1, now) : new Window(w.failures() + 1, w.start()));
    }
}
