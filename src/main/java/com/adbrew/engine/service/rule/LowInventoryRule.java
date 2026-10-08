package com.adbrew.engine.service.rule;

import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.domain.enums.RecommendationAction;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Days of cover = on-hand inventory / trailing daily unit velocity.
 * Advertising into a stockout wastes spend and hurts ranking; pause below 7 days.
 */
@Component
public class LowInventoryRule implements OptimizationRule {

    static final int MIN_DAYS_OF_COVER = 7;

    @Override
    public String name() {
        return "LowInventoryRule";
    }

    @Override
    public Optional<Recommendation> evaluate(ProfitabilityResponse profitability) {
        BigDecimal velocity = profitability.averageDailyUnitsSold();
        if (velocity == null || velocity.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        BigDecimal daysOfCover = BigDecimal.valueOf(profitability.inventoryUnits())
                .divide(velocity, 2, RoundingMode.HALF_UP);

        if (daysOfCover.compareTo(BigDecimal.valueOf(MIN_DAYS_OF_COVER)) >= 0) {
            return Optional.empty();
        }

        String reasoning = String.format(
                "Inventory is %d units against a trailing velocity of %s units/day (%.1f days of cover, below %d). "
                        + "Pause ads until inbound inventory restores cover.",
                profitability.inventoryUnits(),
                velocity.toPlainString(),
                daysOfCover.doubleValue(),
                MIN_DAYS_OF_COVER);

        return Optional.of(Recommendation.builder()
                .campaignId(profitability.campaignId())
                .date(profitability.date())
                .ruleTriggered(name())
                .action(RecommendationAction.PAUSE)
                .reasoning(reasoning)
                .build());
    }
}
