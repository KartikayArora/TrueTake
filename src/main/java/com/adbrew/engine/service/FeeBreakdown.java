package com.adbrew.engine.service;

import java.math.BigDecimal;

public record FeeBreakdown(
        BigDecimal referralFee,
        BigDecimal fbaFee,
        BigDecimal referralFeePercent,
        BigDecimal fbaFeePerUnit
) {
}
