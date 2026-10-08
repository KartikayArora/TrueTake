package com.adbrew.engine.dto.request;

import com.adbrew.engine.seed.DemoIds;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyPerformanceIngestItem(
        @NotBlank(message = "campaignId is required")
        @Schema(
                description = "Seeded demo campaign",
                allowableValues = {
                        DemoIds.CAMP_SERUM,
                        DemoIds.CAMP_MOISTURIZER,
                        DemoIds.CAMP_RETINOL,
                        DemoIds.CAMP_COLLAGEN,
                        DemoIds.CAMP_TONER,
                        DemoIds.CAMP_MAGNESIUM,
                        DemoIds.CAMP_OMEGA,
                        DemoIds.CAMP_CUTTING_BOARD,
                        DemoIds.CAMP_SHEETS,
                        DemoIds.CAMP_DIFFUSER
                },
                example = DemoIds.CAMP_SERUM
        )
        String campaignId,

        @NotNull(message = "date is required")
        @Schema(type = "string", format = "date", example = "2026-09-01", description = "Performance day")
        LocalDate date,

        @Min(value = 0, message = "impressions must be >= 0")
        int impressions,

        @Min(value = 0, message = "clicks must be >= 0")
        int clicks,

        @NotNull(message = "adSpend is required")
        @DecimalMin(value = "0.00", message = "adSpend must be >= 0")
        BigDecimal adSpend,

        @Min(value = 0, message = "unitsSold must be >= 0")
        int unitsSold,

        @NotNull(message = "revenue is required")
        @DecimalMin(value = "0.00", message = "revenue must be >= 0")
        BigDecimal revenue,

        @NotNull(message = "returnsRate is required")
        @DecimalMin(value = "0.00", message = "returnsRate must be >= 0")
        @DecimalMax(value = "1.00", message = "returnsRate must be <= 1")
        BigDecimal returnsRate
) {
}
