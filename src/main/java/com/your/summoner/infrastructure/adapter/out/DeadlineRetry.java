package com.your.summoner.infrastructure.adapter.out;

import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.core.functions.Either;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Retry com prazo total para a operação inteira (ADR 0002 (d)). Usa a configuração de tentativas e de espera
 * do {@link Retry} do Resilience4j, mas, antes de cada espera, para se o prazo não comportá-la. O estouro é
 * lançado aqui, fora de {@code attempt}: o circuit breaker, que fica dentro de {@code attempt}, só vê as
 * tentativas reais e não ganha uma falha a mais.
 * <p>
 * shortcut: o prazo é checado entre tentativas, sem interromper uma em andamento (o Feign não é
 * interrompível). O teto real é o prazo mais o tempo máximo de uma tentativa (conexão + leitura do client).
 * Para um corte exato, passar o tempo restante ao timeout do client.
 */
public final class DeadlineRetry {

    private static final RetryableFailure RETRYABLE = new RetryableFailure();

    private DeadlineRetry() {
    }

    /** Falha na subida se a configuração for inválida. */
    public static Duration requirePositive(String property, Duration deadline) {
        if (deadline == null || deadline.isZero() || deadline.isNegative()) {
            throw new IllegalArgumentException(property + " must be greater than zero, got " + deadline);
        }
        return deadline;
    }

    public static <T> T call(String service, Duration totalDeadline, Retry retry, Supplier<T> attempt) {
        long limit = System.nanoTime() + totalDeadline.toNanos();
        var config = retry.getRetryConfig();
        for (int number = 1; ; number++) {
            try {
                return attempt.get();
            } catch (RuntimeException failure) {
                if (number >= config.getMaxAttempts() || !RETRYABLE.test(failure)) {
                    throw failure;
                }
                long wait = TimeUnit.MILLISECONDS.toNanos(config.getIntervalBiFunction().apply(number, Either.left(failure)));
                if (limit - System.nanoTime() <= wait) {
                    throw new ExternalServiceUnavailableException(service,
                            "%s did not answer within the total deadline of %s".formatted(service, totalDeadline),
                            failure);
                }
                sleep(service, wait);
            }
        }
    }

    private static void sleep(String service, long nanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceUnavailableException(service, "Interrupted while calling " + service, e);
        }
    }
}
