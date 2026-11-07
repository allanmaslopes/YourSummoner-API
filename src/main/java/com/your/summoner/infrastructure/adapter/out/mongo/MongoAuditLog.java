package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.AuditLog;
import com.your.summoner.domain.model.AuditEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class MongoAuditLog implements AuditLog {
    private final MongoTemplate mongo;

    public MongoAuditLog(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void record(String actor, String action, String target, String ip) {
        mongo.insert(new AuditDocument(null, actor, action, target, ip, Instant.now()));
    }

    @Override
    public List<AuditEntry> recent(int limit) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "at")).limit(limit);
        return mongo.find(query, AuditDocument.class).stream()
                .map(d -> new AuditEntry(d.actor(), d.action(), d.target(), d.ip(), d.at()))
                .toList();
    }
}
