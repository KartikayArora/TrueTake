package com.adbrew.engine.dto.response;

import java.math.BigDecimal;

public record MoneyTotals(
        BigDecimal revenue,
        BigDecimal adSpend,
        BigDecimal trueProfit,
        BigDecimal profitMargin,
        BigDecimal acos
) {
}
