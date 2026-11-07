package com.your.summoner.application.port.in;

public interface SyncPatchesUseCase {

    /** Lê o versions.json, ingere o patch mais novo e só então o ativa. Falha de ingestão mantém o patch anterior. */
    void syncLatest();

    /** Ingere um patch conhecido no locale configurado, sem ativá-lo. */
    void ingest(String version);
}
