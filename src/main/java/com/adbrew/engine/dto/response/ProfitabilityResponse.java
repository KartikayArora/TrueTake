package com.adbrew.engine.dto.response;

import com.adbrew.engine.domain.enums.CampaignStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "True-profitability breakdown for one campaign on one day")
public record ProfitabilityResponse(
        String accountId,
        String campaignId,
        String campaignName,
        CampaignStatus campaignStatus,
        String productId,
        String sku,
        String productName,
        String category,
        LocalDate date,
        int impressions,
        int clicks,
        int unitsSold,
        int inventoryUnits,
        BigDecimal averageDailyUnitsSold,
        BigDecimal currentBid,
        BigDecimal revenue,
        BigDecimal adSpend,
        BigDecimal cogsTotal,
        BigDecimal referralFee,
        BigDecimal fbaFee,
        BigDecimal returnsCost,
        BigDecimal trueProfit,
        BigDecimal profitMargin,
        BigDecimal profitPerClick,
        BigDecimal acos
) {
}
