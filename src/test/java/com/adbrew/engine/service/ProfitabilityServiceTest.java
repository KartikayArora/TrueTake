package com.adbrew.engine.service;

import com.adbrew.engine.domain.Campaign;
import com.adbrew.engine.domain.DailyPerformance;
import com.adbrew.engine.domain.FeeSchedule;
import com.adbrew.engine.domain.FbaWeightTier;
import com.adbrew.engine.domain.Product;
import com.adbrew.engine.domain.enums.CampaignStatus;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import com.adbrew.engine.exception.ResourceNotFoundException;
import com.adbrew.engine.repository.AccountRepository;
import com.adbrew.engine.repository.CampaignRepository;
import com.adbrew.engine.repository.DailyPerformanceRepository;
import com.adbrew.engine.repository.FeeScheduleRepository;
import com.adbrew.engine.repository.ProductRepository;
import com.adbrew.engine.util.MoneyMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitabilityServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 31);

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private DailyPerformanceRepository dailyPerformanceRepository;
    @Mock
    private FeeScheduleRepository feeScheduleRepository;

    private ProfitabilityService profitabilityService;

    private Campaign campaign;
    private Product product;
    private DailyPerformance performance;

    @BeforeEach
    void setUp() {
        FeeCalculationService feeCalculationService = new FeeCalculationService(feeScheduleRepository);
        profitabilityService = new ProfitabilityService(
                accountRepository,
                campaignRepository,
                productRepository,
                dailyPerformanceRepository,
                feeCalculationService
        );

        campaign = Campaign.builder()
                .id("camp-serum")
                .accountId("acc-lumina")
                .productId("prod-serum")
                .name("SP - Vitamin C Serum")
                .currentBid(MoneyMath.money("1.15"))
                .status(CampaignStatus.ACTIVE)
                .build();

        product = Product.builder()
                .id("prod-serum")
                .accountId("acc-lumina")
                .sku("LUM-SERUM-30")
                .name("Vitamin C Brightening Serum 30ml")
                .cogs(MoneyMath.money("5.00"))
                .weightTierOz(6)
                .inventoryUnits(420)
                .category("Beauty & Personal Care")
                .build();

        performance = DailyPerformance.builder()
                .id("perf-1")
                .campaignId("camp-serum")
                .date(DATE)
                .impressions(8000)
                .clicks(50)
                .adSpend(MoneyMath.money("40.00"))
                .unitsSold(10)
                .revenue(MoneyMath.money("250.00"))
                .returnsRate(new BigDecimal("0.05"))
                .build();
    }

    @Test
    void computesTrueProfitAfterCogsFeesReturnsAndAdSpend() {
        stubHappyPath();
        when(dailyPerformanceRepository.findByCampaignIdAndDateBetween(
                "camp-serum", DATE.minusDays(6), DATE))
                .thenReturn(List.of(performance));

        ProfitabilityResponse result = profitabilityService.forCampaignOnDate("camp-serum", DATE);

        // 250 - (5*10) - (250*0.15) - (3.45*10) - (250*0.05) - 40 = 75.50
        assertThat(result.trueProfit()).isEqualByComparingTo("75.50");
        assertThat(result.cogsTotal()).isEqualByComparingTo("50.00");
        assertThat(result.referralFee()).isEqualByComparingTo("37.50");
        assertThat(result.fbaFee()).isEqualByComparingTo("34.50");
        assertThat(result.returnsCost()).isEqualByComparingTo("12.50");
        assertThat(result.profitMargin()).isEqualByComparingTo("0.3020");
        assertThat(result.profitPerClick()).isEqualByComparingTo("1.5100");
        assertThat(result.acos()).isEqualByComparingTo("0.1600");
        assertThat(result.averageDailyUnitsSold()).isEqualByComparingTo("10.0000");
        assertThat(result.sku()).isEqualTo("LUM-SERUM-30");
    }

    @Test
    void reportsNegativeTrueProfitWhenAdSpendExceedsContribution() {
        performance.setAdSpend(MoneyMath.money("130.00"));
        stubHappyPath();
        when(dailyPerformanceRepository.findByCampaignIdAndDateBetween(
                "camp-serum", DATE.minusDays(6), DATE))
                .thenReturn(List.of(performance));

        ProfitabilityResponse result = profitabilityService.forCampaignOnDate("camp-serum", DATE);

        // 250 - 50 - 37.50 - 34.50 - 12.50 - 130 = -14.50
        assertThat(result.trueProfit()).isEqualByComparingTo("-14.50");
        assertThat(result.profitPerClick()).isEqualByComparingTo("-0.2900");
        assertThat(result.profitMargin()).isEqualByComparingTo("-0.0580");
    }

    @Test
    void throwsWhenCampaignIsMissing() {
        when(campaignRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profitabilityService.forCampaignOnDate("missing", DATE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Campaign");
    }

    @Test
    void throwsWhenPerformanceIsMissing() {
        when(campaignRepository.findById("camp-serum")).thenReturn(Optional.of(campaign));
        when(dailyPerformanceRepository.findByCampaignIdAndDate("camp-serum", DATE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profitabilityService.forCampaignOnDate("camp-serum", DATE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DailyPerformance");
    }

    private void stubHappyPath() {
        when(campaignRepository.findById("camp-serum")).thenReturn(Optional.of(campaign));
        when(dailyPerformanceRepository.findByCampaignIdAndDate("camp-serum", DATE))
                .thenReturn(Optional.of(performance));
        when(productRepository.findById("prod-serum")).thenReturn(Optional.of(product));
        when(feeScheduleRepository.findByCategory("Beauty & Personal Care"))
                .thenReturn(Optional.of(FeeSchedule.builder()
                        .category("Beauty & Personal Care")
                        .referralFeePercent(new BigDecimal("0.15"))
                        .fbaFeeByWeightTier(List.of(
                                FbaWeightTier.builder().maxOz(6).fee(MoneyMath.money("3.45")).build()
                        ))
                        .build()));
    }
}
