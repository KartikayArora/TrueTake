package com.adbrew.engine.service.rule;

import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.domain.enums.RecommendationAction;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Negative profit per click means every additional click is destroying margin.
 * Deeply unprofitable campaigns are paused; milder losses get a bid cut.
 */
@Component
public class LowProfitRule implements OptimizationRule {

    static final BigDecimal SEVERE_MARGIN = new BigDecimal("-0.20");

    @Override
    public String name() {
        return "LowProfitRule";
    }

    @Override
    public Optional<Recommendation> evaluate(ProfitabilityResponse profitability) {
        BigDecimal profitPerClick = profitability.profitPerClick();
        if (profitPerClick == null || profitPerClick.compareTo(BigDecimal.ZERO) >= 0) {
            return Optional.empty();
        }

        boolean severe = profitability.profitMargin() != null
                && profitability.profitMargin().compareTo(SEVERE_MARGIN) < 0;
        RecommendationAction action = severe ? RecommendationAction.PAUSE : RecommendationAction.LOWER_BID;

        String reasoning = severe
                ? String.format(
                "Profit per click is %s and profit margin is %s (below -20%%). "
                        + "True profit after COGS, Amazon fees, returns and ad spend is %s on %s revenue. Pause the campaign.",
                profitPerClick.toPlainString(),
                profitability.profitMargin().toPlainString(),
                profitability.trueProfit().toPlainString(),
                profitability.revenue().toPlainString())
                : String.format(
                "Profit per click is %s (negative). True profit is %s on %s ad spend. Lower the bid to improve unit economics.",
                profitPerClick.toPlainString(),
                profitability.trueProfit().toPlainString(),
                profitability.adSpend().toPlainString());

        return Optional.of(base(profitability, action, reasoning));
    }

    private Recommendation base(ProfitabilityResponse profitability, RecommendationAction action, String reasoning) {
        return Recommendation.builder()
                .campaignId(profitability.campaignId())
                .date(profitability.date())
                .ruleTriggered(name())
                .action(action)
                .reasoning(reasoning)
                .build();
    }
}
