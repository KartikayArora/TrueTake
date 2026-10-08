package com.adbrew.engine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyMath {

    public static final int MONEY_SCALE = 2;
    public static final int RATIO_SCALE = 4;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyMath() {
    }

    public static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, ROUNDING);
    }

    public static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(MONEY_SCALE, ROUNDING);
    }

    public static BigDecimal ratio(BigDecimal value) {
        return value.setScale(RATIO_SCALE, ROUNDING);
    }

    public static BigDecimal multiply(BigDecimal left, BigDecimal right) {
        return money(left.multiply(right));
    }

    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, RATIO_SCALE, ROUNDING);
    }

    public static BigDecimal divide(BigDecimal numerator, int count) {
        if (count == 0) {
            return null;
        }
        return divide(numerator, BigDecimal.valueOf(count));
    }
}
