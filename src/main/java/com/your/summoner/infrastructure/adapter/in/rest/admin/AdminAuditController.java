package com.your.summoner.infrastructure.adapter.in.rest.admin;

import com.your.summoner.application.port.in.ViewAuditUseCase;
import com.your.summoner.domain.model.AuditEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminAuditController {
    private final ViewAuditUseCase audit;

    public AdminAuditController(ViewAuditUseCase audit) {
        this.audit = audit;
    }

    @GetMapping("/admin/audit")
    List<AuditEntry> recent(@RequestParam(defaultValue = "50") int limit) {
        return audit.recent(limit);
    }
}
