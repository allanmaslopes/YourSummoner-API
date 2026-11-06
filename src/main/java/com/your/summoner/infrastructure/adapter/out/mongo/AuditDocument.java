package com.your.summoner.infrastructure.adapter.out.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("admin_audit")
public record AuditDocument(@Id String id, String actor, String action, String target, String ip, Instant at) {
}
