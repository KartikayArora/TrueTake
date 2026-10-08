package com.adbrew.engine.service;

import com.adbrew.engine.domain.FeeSchedule;
import com.adbrew.engine.domain.FbaWeightTier;
import com.adbrew.engine.domain.Product;
import com.adbrew.engine.exception.UnprocessableRequestException;
import com.adbrew.engine.repository.FeeScheduleRepository;
import com.adbrew.engine.util.MoneyMath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class FeeCalculationService {

    private final FeeScheduleRepository feeScheduleRepository;

    /**
     * Resolves Amazon marketplace fees for a product given actual units and revenue.
     *
     * <p>Referral fee is a category percentage of item revenue (not of COGS).
     * FBA fee is a per-unit pick/pack/ship charge looked up from the product's
     * shipping-weight tier.
     */
    public FeeBreakdown calculate(Product product, int unitsSold, BigDecimal revenue) {
        FeeSchedule schedule = feeScheduleRepository.findByCategory(product.getCategory())
                .orElseThrow(() -> new UnprocessableRequestException(
                        "No fee schedule for category: " + product.getCategory()));

        BigDecimal referralFee = MoneyMath.multiply(revenue, schedule.getReferralFeePercent());
        BigDecimal fbaPerUnit = lookupFbaFee(schedule, product.getWeightTierOz());
        BigDecimal fbaFee = MoneyMath.multiply(fbaPerUnit, BigDecimal.valueOf(unitsSold));

        return new FeeBreakdown(referralFee, fbaFee, schedule.getReferralFeePercent(), fbaPerUnit);
    }

    BigDecimal lookupFbaFee(FeeSchedule schedule, int weightTierOz) {
        return schedule.getFbaFeeByWeightTier().stream()
                .sorted(Comparator.comparingInt(FbaWeightTier::getMaxOz))
                .filter(tier -> weightTierOz <= tier.getMaxOz())
                .findFirst()
                .map(FbaWeightTier::getFee)
                .orElseThrow(() -> new UnprocessableRequestException(
                        "No FBA tier covers weight " + weightTierOz + " oz in category "
                                + schedule.getCategory()));
    }
}
