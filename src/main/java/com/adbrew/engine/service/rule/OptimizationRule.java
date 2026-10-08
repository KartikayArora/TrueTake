package com.adbrew.engine.service.rule;

import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.dto.response.ProfitabilityResponse;

import java.util.Optional;

public interface OptimizationRule {

    String name();

    Optional<Recommendation> evaluate(ProfitabilityResponse profitability);
}
