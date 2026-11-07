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

    /** Atomically reserves an attempt, or returns the seconds until the address may try again. */
    public OptionalLong tryAcquire(String ip) {
        Instant now = clock.instant();
        long[] retryAfter = {0};
        failures.asMap().compute(ip, (k, w) -> {
            if (w == null || !now.isBefore(w.start().plus(WINDOW))) {
                return new Window(1, now);
            }
            if (w.failures() >= MAX_FAILURES) {
                retryAfter[0] = Math.max(1, Duration.between(now, w.start().plus(WINDOW)).toSeconds() + 1);
                return w;
            }
            return new Window(w.failures() + 1, w.start());
        });
        return retryAfter[0] == 0 ? OptionalLong.empty() : OptionalLong.of(retryAfter[0]);
    }

    public void release(String ip) {
        failures.invalidate(ip);
    }
}
