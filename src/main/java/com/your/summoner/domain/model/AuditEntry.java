package com.your.summoner.domain.model;

import java.time.Instant;

public record AuditEntry(String actor, String action, String target, String ip, Instant at) {
}
