package com.your.summoner.application.port.out;

import com.your.summoner.domain.model.AuditEntry;

import java.util.List;

/** Append-only admin audit trail. Never pass secret values, passwords or codes. */
public interface AuditLog {
    void record(String actor, String action, String target, String ip);

    List<AuditEntry> recent(int limit);
}
