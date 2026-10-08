package com.adbrew.engine.dto.response;

import java.util.List;

public record CampaignProfitabilitySummary(
        String campaignId,
        String campaignName,
        String sku,
        String productName,
        MoneyTotals totals,
        List<ProfitabilityResponse> daily
) {
}
