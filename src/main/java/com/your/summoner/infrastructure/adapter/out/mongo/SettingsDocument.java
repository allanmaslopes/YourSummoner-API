package com.your.summoner.infrastructure.adapter.out.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("app_settings")
public record SettingsDocument(@Id String id, String locale, String defaultPlatform, Instant updatedAt, String updatedBy) {
}
