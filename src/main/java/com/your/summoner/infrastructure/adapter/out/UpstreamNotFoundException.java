package com.your.summoner.infrastructure.adapter.out;

/**
 * 404 (ou 403 do Data Dragon) de um serviço externo. Fica na infraestrutura: cada adapter decide
 * se isso vira {@code Optional.empty()} ou lista vazia.
 */
public class UpstreamNotFoundException extends RuntimeException {

    public UpstreamNotFoundException(String message) {
        super(message);
    }
}
