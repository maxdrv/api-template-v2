package com.home.project.template.configuration;

import java.time.Instant;

public record Configuration(Long id, Instant createdAt, Instant updatedAt, String key, String value) {
}
