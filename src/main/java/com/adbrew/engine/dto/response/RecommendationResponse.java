package com.adbrew.engine.dto.response;

import com.adbrew.engine.domain.enums.RecommendationAction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Bid/budget recommendation produced by an optimization rule")
public record RecommendationResponse(
        String id,
        String campaignId,
        LocalDate date,
        String ruleTriggered,
        RecommendationAction action,
        String reasoning,
        Instant createdAt
) {
}
