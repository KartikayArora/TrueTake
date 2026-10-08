package com.adbrew.engine.dto.response;

import java.time.LocalDate;
import java.util.List;

public record RecommendationRunResponse(
        String accountId,
        LocalDate evaluatedDate,
        String message,
        int recommendationsCreated,
        List<RecommendationResponse> recommendations
) {
}
