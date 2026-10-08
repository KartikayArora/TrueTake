package com.adbrew.engine.service.rule;

import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.domain.enums.RecommendationAction;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import com.adbrew.engine.support.ProfitabilityFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LowProfitRuleTest {

    private final LowProfitRule rule = new LowProfitRule();

    @Test
    void recommendsLowerBidWhenProfitPerClickIsNegativeButMarginIsMild() {
        ProfitabilityResponse snapshot = ProfitabilityFixtures.response(
                new BigDecimal("-14.50"),
                new BigDecimal("-0.0580"),
                new BigDecimal("-0.2900"),
                420,
                new BigDecimal("10.0000"));

        Optional<Recommendation> result = rule.evaluate(snapshot);

        assertThat(result).isPresent();
        assertThat(result.get().getAction()).isEqualTo(RecommendationAction.LOWER_BID);
        assertThat(result.get().getRuleTriggered()).isEqualTo("LowProfitRule");
        assertThat(result.get().getCampaignId()).isEqualTo("camp-serum");
        assertThat(result.get().getReasoning()).contains("negative");
    }

    @Test
    void recommendsPauseWhenMarginIsWorseThanNegativeTwentyPercent() {
        ProfitabilityResponse snapshot = ProfitabilityFixtures.response(
                new BigDecimal("-80.00"),
                new BigDecimal("-0.3200"),
                new BigDecimal("-1.6000"),
                420,
                new BigDecimal("10.0000"));

        Optional<Recommendation> result = rule.evaluate(snapshot);

        assertThat(result).isPresent();
        assertThat(result.get().getAction()).isEqualTo(RecommendationAction.PAUSE);
        assertThat(result.get().getReasoning()).contains("Pause");
    }

    @Test
    void staysSilentWhenProfitPerClickIsNonNegative() {
        ProfitabilityResponse snapshot = ProfitabilityFixtures.response(
                new BigDecimal("75.50"),
                new BigDecimal("0.3020"),
                new BigDecimal("1.5100"),
                420,
                new BigDecimal("10.0000"));

        assertThat(rule.evaluate(snapshot)).isEmpty();
    }
}
