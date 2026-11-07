package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.AuditEntry;

import java.util.List;

public interface ViewAuditUseCase {
    List<AuditEntry> recent(int limit);
}
