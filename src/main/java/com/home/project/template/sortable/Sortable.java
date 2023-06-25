package com.home.project.template.sortable;

import java.time.Instant;

public record Sortable(
        Long id,
        Instant createdAt,
        Instant updatedAt,
        Long soringCenterId,
        String status,
        String type,
        Long stageId,
        String barcode,
        Long archiveId
) {
}
