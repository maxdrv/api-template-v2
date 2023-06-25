package com.home.project.template.generator;

import java.time.Instant;

public record Sortable(Long id, Instant createdAt, Instant updatedAt, String status, String type, Long stageId, String barcode) {
}
