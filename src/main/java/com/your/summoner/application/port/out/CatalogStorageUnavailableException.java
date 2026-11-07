package com.your.summoner.application.port.out;

/** O armazenamento do catálogo (MongoDB) está indisponível; o catálogo cai para leitura direta do Data Dragon. */
public class CatalogStorageUnavailableException extends RuntimeException {

    public CatalogStorageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
