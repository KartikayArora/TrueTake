package com.adbrew.engine.service.rule;

import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.domain.enums.RecommendationAction;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Strong true-profit margin with positive profit-per-click is a signal to buy more traffic.
 */
@Component
public class HighPerformerRule implements OptimizationRule {

    static final BigDecimal STRONG_MARGIN = new BigDecimal("0.20");
    static final BigDecimal MIN_PROFIT_PER_CLICK = new BigDecimal("0.50");

    @Override
    public String name() {
        return "HighPerformerRule";
    }

    @Override
    public Optional<Recommendation> evaluate(ProfitabilityResponse profitability) {
        BigDecimal margin = profitability.profitMargin();
        BigDecimal profitPerClick = profitability.profitPerClick();
        if (margin == null || profitPerClick == null) {
            return Optional.empty();
        }
        if (margin.compareTo(STRONG_MARGIN) < 0 || profitPerClick.compareTo(MIN_PROFIT_PER_CLICK) < 0) {
            return Optional.empty();
        }

        String reasoning = String.format(
                "True profit margin is %s with profit per click of %s (true profit %s on %s revenue). "
                        + "Raise the bid to capture more profitable volume.",
                margin.toPlainString(),
                profitPerClick.toPlainString(),
                profitability.trueProfit().toPlainString(),
                profitability.revenue().toPlainString());

        return Optional.of(Recommendation.builder()
                .campaignId(profitability.campaignId())
                .date(profitability.date())
                .ruleTriggered(name())
                .action(RecommendationAction.RAISE_BID)
                .reasoning(reasoning)
                .build());
    }
}
