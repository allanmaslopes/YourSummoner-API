package com.your.summoner.infrastructure.adapter.in.rest.dto;

import java.util.Map;

/** Patch conhecido; {@code statusByLocale} mapeia o locale ao estado da ingestão (NOT_INGESTED, INGESTING, READY, FAILED). */
public record PatchResponse(String version, boolean active, Map<String, String> statusByLocale) {
}
