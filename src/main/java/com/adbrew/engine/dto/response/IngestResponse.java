package com.adbrew.engine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a bulk daily-performance ingest")
public record IngestResponse(
        int accepted,
        int inserted,
        int updated
) {
}
