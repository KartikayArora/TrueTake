package com.adbrew.engine.service;

import com.adbrew.engine.domain.FeeSchedule;
import com.adbrew.engine.domain.FbaWeightTier;
import com.adbrew.engine.domain.Product;
import com.adbrew.engine.exception.UnprocessableRequestException;
import com.adbrew.engine.repository.FeeScheduleRepository;
import com.adbrew.engine.util.MoneyMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {

    @Mock
    private FeeScheduleRepository feeScheduleRepository;

    @InjectMocks
    private FeeCalculationService feeCalculationService;

    private Product product;
    private FeeSchedule schedule;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("prod-serum")
                .category("Beauty & Personal Care")
                .weightTierOz(6)
                .cogs(MoneyMath.money("5.00"))
                .build();

        schedule = FeeSchedule.builder()
                .category("Beauty & Personal Care")
                .referralFeePercent(new BigDecimal("0.15"))
                .fbaFeeByWeightTier(List.of(
                        FbaWeightTier.builder().maxOz(4).fee(MoneyMath.money("3.42")).build(),
                        FbaWeightTier.builder().maxOz(6).fee(MoneyMath.money("3.45")).build(),
                        FbaWeightTier.builder().maxOz(8).fee(MoneyMath.money("3.54")).build()
                ))
                .build();
    }

    @Test
    void appliesReferralPercentToRevenueAndFbaFeeByWeightTier() {
        when(feeScheduleRepository.findByCategory("Beauty & Personal Care"))
                .thenReturn(Optional.of(schedule));

        FeeBreakdown breakdown = feeCalculationService.calculate(
                product, 10, MoneyMath.money("250.00"));

        assertThat(breakdown.referralFee()).isEqualByComparingTo("37.50");
        assertThat(breakdown.fbaFeePerUnit()).isEqualByComparingTo("3.45");
        assertThat(breakdown.fbaFee()).isEqualByComparingTo("34.50");
    }

    @Test
    void throwsWhenCategoryHasNoSchedule() {
        when(feeScheduleRepository.findByCategory("Beauty & Personal Care"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> feeCalculationService.calculate(product, 10, MoneyMath.money("250.00")))
                .isInstanceOf(UnprocessableRequestException.class)
                .hasMessageContaining("No fee schedule");
    }
}
