package com.your.summoner.infrastructure.adapter.out;

import org.springframework.cache.Cache;

import java.util.function.Supplier;

/**
 * Cache programático que não guarda falhas: se o loader lança (ex.: {@link UpstreamNotFoundException}),
 * nada entra no cache e a exceção original chega ao chamador, que decide o valor vazio fora do cache.
 */
public final class CachedCalls {

    private CachedCalls() {
    }

    public static <V> V get(Cache cache, Object key, Supplier<V> loader) {
        try {
            return cache.get(key, loader::get);
        } catch (Cache.ValueRetrievalException e) {
            if (e.getCause() instanceof RuntimeException r) {
                throw r;
            }
            throw e;
        }
    }
}
