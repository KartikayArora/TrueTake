package com.adbrew.engine.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AccountProfitabilitySummary(
        String accountId,
        String accountName,
        LocalDate from,
        LocalDate to,
        MoneyTotals totals,
        List<CampaignProfitabilitySummary> campaigns
) {
}
