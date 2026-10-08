package com.adbrew.engine.support;

import com.adbrew.engine.domain.enums.CampaignStatus;
import com.adbrew.engine.dto.response.ProfitabilityResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ProfitabilityFixtures {

    private ProfitabilityFixtures() {
    }

    public static ProfitabilityResponse response(
            BigDecimal trueProfit,
            BigDecimal profitMargin,
            BigDecimal profitPerClick,
            int inventoryUnits,
            BigDecimal averageDailyUnitsSold) {
        return new ProfitabilityResponse(
                "acc-lumina",
                "camp-serum",
                "SP - Vitamin C Serum",
                CampaignStatus.ACTIVE,
                "prod-serum",
                "LUM-SERUM-30",
                "Vitamin C Brightening Serum 30ml",
                "Beauty & Personal Care",
                LocalDate.of(2026, 8, 31),
                8000,
                50,
                10,
                inventoryUnits,
                averageDailyUnitsSold,
                new BigDecimal("1.15"),
                new BigDecimal("250.00"),
                new BigDecimal("40.00"),
                new BigDecimal("50.00"),
                new BigDecimal("37.50"),
                new BigDecimal("34.50"),
                new BigDecimal("12.50"),
                trueProfit,
                profitMargin,
                profitPerClick,
                new BigDecimal("0.1600")
        );
    }
}
