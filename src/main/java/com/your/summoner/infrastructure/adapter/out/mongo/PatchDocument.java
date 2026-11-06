package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.domain.model.PatchStatus;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("patches")
public record PatchDocument(String version, boolean active, int order,
                            Map<String, PatchStatus> statusByLocale,
                            Map<String, Instant> lockedAtByLocale,
                            Map<String, Instant> finishedAtByLocale) {
}
