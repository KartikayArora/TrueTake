package com.adbrew.engine.service.rule;

import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.domain.enums.RecommendationAction;
import com.adbrew.engine.support.ProfitabilityFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LowInventoryRuleTest {

    private final LowInventoryRule rule = new LowInventoryRule();

    @Test
    void recommendsPauseWhenDaysOfCoverAreBelowSeven() {
        Optional<Recommendation> result = rule.evaluate(ProfitabilityFixtures.response(
                new BigDecimal("20.00"),
                new BigDecimal("0.1000"),
                new BigDecimal("0.4000"),
                15,
                new BigDecimal("8.0000")));

        assertThat(result).isPresent();
        assertThat(result.get().getAction()).isEqualTo(RecommendationAction.PAUSE);
        assertThat(result.get().getRuleTriggered()).isEqualTo("LowInventoryRule");
        assertThat(result.get().getReasoning()).contains("days of cover");
    }

    @Test
    void staysSilentWhenCoverIsHealthy() {
        assertThat(rule.evaluate(ProfitabilityFixtures.response(
                new BigDecimal("75.50"),
                new BigDecimal("0.3020"),
                new BigDecimal("1.5100"),
                420,
                new BigDecimal("10.0000")))).isEmpty();
    }

    @Test
    void staysSilentWhenThereIsNoSalesVelocity() {
        assertThat(rule.evaluate(ProfitabilityFixtures.response(
                new BigDecimal("-5.00"),
                new BigDecimal("-0.0200"),
                new BigDecimal("-0.1000"),
                5,
                BigDecimal.ZERO))).isEmpty();
    }
}
