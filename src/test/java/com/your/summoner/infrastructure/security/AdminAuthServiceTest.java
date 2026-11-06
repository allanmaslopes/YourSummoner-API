package com.your.summoner.infrastructure.security;

import com.your.summoner.application.port.out.AuditLog;
import com.your.summoner.application.port.out.SecretStore;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthServiceTest {
    private static final String USER = "admin";
    private static final String HASH = "configured-hash";

    @Test
    void concurrentBurstIsLimitedToFive() throws Exception {
        AtomicInteger passwordChecks = new AtomicInteger();
        PasswordEncoder encoder = passwordEncoder((raw, hash) -> {
            passwordChecks.incrementAndGet();
            return false;
        });
        AdminAuthService service = service(encoder);
        CountDownLatch ready = new CountDownLatch(20);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Throwable>> attempts = java.util.stream.IntStream.range(0, 20)
                    .mapToObj(i -> executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        try {
                            service.login(USER, "wrong", "", "127.0.0.1");
                            return null;
                        } catch (Throwable failure) {
                            return failure;
                        }
                    })).toList();
            ready.await();
            start.countDown();

            List<Throwable> failures = attempts.stream().map(AdminAuthServiceTest::get).toList();
            assertEquals(5, failures.stream().filter(InvalidCredentialsException.class::isInstance).count());
            assertEquals(15, failures.stream().filter(TooManyLoginAttemptsException.class::isInstance).count());
        }
        assertEquals(5, passwordChecks.get());
    }

    @Test
    void passwordMatchesConfiguredHashExactlyOnceForEveryCredentialPair() {
        PasswordEncoder encoder = passwordEncoder((raw, hash) -> raw.toString().equals("correct"));

        AdminAuthService valid = service(encoder);
        valid.login(USER, "correct", "", "127.0.0.1");
        assertThrows(InvalidCredentialsException.class,
                () -> service(encoder).login(USER, "wrong", "", "127.0.0.2"));
        assertThrows(InvalidCredentialsException.class,
                () -> service(encoder).login("wrong-user", "anything", "", "127.0.0.3"));

        verify(encoder, times(1)).matches("correct", HASH);
        verify(encoder, times(1)).matches("wrong", HASH);
        verify(encoder, times(1)).matches("anything", HASH);
        verify(encoder, times(3)).matches(any(), any());
    }

    private static AdminAuthService service(PasswordEncoder encoder) {
        SecretStore secrets = mock(SecretStore.class);
        when(secrets.find(any())).thenReturn(java.util.Optional.empty());
        return new AdminAuthService(
                new AdminProperties(USER, HASH, List.of("127.0.0.1/32")),
                MasterKey.parse(Base64.getEncoder().encodeToString(new byte[32])),
                secrets,
                mock(AuditLog.class),
                new LoginRateLimiter(Clock.systemUTC()),
                Clock.systemUTC(),
                encoder);
    }

    private static PasswordEncoder passwordEncoder(PasswordMatcher matcher) {
        return spy(new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return matcher.matches(rawPassword, encodedPassword);
            }
        });
    }

    private static Throwable get(Future<Throwable> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } catch (ExecutionException e) {
            throw new AssertionError(e.getCause());
        }
    }

    @FunctionalInterface
    private interface PasswordMatcher {
        boolean matches(CharSequence rawPassword, String encodedPassword);
    }
}
