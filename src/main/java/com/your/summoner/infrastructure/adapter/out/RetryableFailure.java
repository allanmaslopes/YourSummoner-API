package com.your.summoner.infrastructure.adapter.out;

import com.your.summoner.domain.exception.ExternalServiceException;

import java.util.function.Predicate;

/**
 * Política de retry dos adapters de saída: só falhas transitórias (5xx, timeout, conexão).
 * Nunca 4xx — 429 é propagado ao chamador com o Retry-After.
 */
public class RetryableFailure implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable failure) {
        return failure instanceof ExternalServiceException e && e.isRetryable();
    }
}
