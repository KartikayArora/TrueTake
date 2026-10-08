package com.adbrew.engine.service;

import com.adbrew.engine.domain.Account;
import com.adbrew.engine.domain.Campaign;
import com.adbrew.engine.domain.DailyPerformance;
import com.adbrew.engine.domain.Product;
import com.adbrew.engine.dto.response.AccountProfitabilitySummary;
import com.adbrew.engine.dto.response.CampaignProfitabilitySummary;
import com.adbrew.engine.dto.response.MoneyTotals;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import com.adbrew.engine.exception.ResourceNotFoundException;
import com.adbrew.engine.repository.AccountRepository;
import com.adbrew.engine.repository.CampaignRepository;
import com.adbrew.engine.repository.DailyPerformanceRepository;
import com.adbrew.engine.repository.ProductRepository;
import com.adbrew.engine.util.MoneyMath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfitabilityService {

    private static final int VELOCITY_WINDOW_DAYS = 7;

    private final AccountRepository accountRepository;
    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final DailyPerformanceRepository dailyPerformanceRepository;
    private final FeeCalculationService feeCalculationService;

    public ProfitabilityResponse forCampaignOnDate(String campaignId, LocalDate date) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignId));

        LocalDate targetDate = date != null
                ? date
                : dailyPerformanceRepository.findTopByCampaignIdOrderByDateDesc(campaignId)
                .map(DailyPerformance::getDate)
                .orElseThrow(() -> new ResourceNotFoundException("No performance data for campaign " + campaignId));

        DailyPerformance performance = dailyPerformanceRepository
                .findByCampaignIdAndDate(campaignId, targetDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DailyPerformance not found for campaign " + campaignId + " on " + targetDate));

        return compute(campaign, performance);
    }

    public AccountProfitabilitySummary forAccount(String accountId, LocalDate from, LocalDate to) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        LocalDate rangeTo = to != null ? to : LocalDate.now();
        LocalDate rangeFrom = from != null ? from : rangeTo.minusDays(29);

        if (rangeFrom.isAfter(rangeTo)) {
            throw new IllegalArgumentException("from must be on or before to");
        }

        List<Campaign> campaigns = campaignRepository.findByAccountId(accountId);
        if (campaigns.isEmpty()) {
            return new AccountProfitabilitySummary(
                    account.getId(), account.getName(), rangeFrom, rangeTo, zeroTotals(), List.of());
        }

        List<String> campaignIds = campaigns.stream().map(Campaign::getId).toList();
        Map<String, List<DailyPerformance>> byCampaign = dailyPerformanceRepository
                .findByCampaignIdInAndDateBetween(campaignIds, rangeFrom, rangeTo)
                .stream()
                .collect(Collectors.groupingBy(DailyPerformance::getCampaignId));

        List<CampaignProfitabilitySummary> campaignSummaries = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            List<DailyPerformance> rows = byCampaign.getOrDefault(campaign.getId(), List.of());
            if (rows.isEmpty()) {
                continue;
            }
            List<ProfitabilityResponse> daily = rows.stream()
                    .sorted(Comparator.comparing(DailyPerformance::getDate))
                    .map(row -> compute(campaign, row))
                    .toList();
            campaignSummaries.add(new CampaignProfitabilitySummary(
                    campaign.getId(),
                    campaign.getName(),
                    daily.getFirst().sku(),
                    daily.getFirst().productName(),
                    sum(daily),
                    daily
            ));
        }

        List<ProfitabilityResponse> allDays = campaignSummaries.stream()
                .flatMap(summary -> summary.daily().stream())
                .toList();

        return new AccountProfitabilitySummary(
                account.getId(),
                account.getName(),
                rangeFrom,
                rangeTo,
                sum(allDays),
                campaignSummaries
        );
    }

    /**
     * True profitability is ACOS/ROAS with the rest of the P&amp;L attached.
     *
     * <pre>
     * trueProfit = revenue
     *            - (cogs * unitsSold)
     *            - referralFee          // category % of revenue
     *            - fbaFee               // per-unit fulfillment * unitsSold
     *            - (revenue * returnsRate)
     *            - adSpend
     * </pre>
     *
     * A campaign can look healthy on ROAS and still lose money once Amazon
     * take-rates, fulfillment, COGS and returns are included. That gap is the
     * point of this engine.
     */
    ProfitabilityResponse compute(Campaign campaign, DailyPerformance performance) {
        Product product = productRepository.findById(campaign.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", campaign.getProductId()));

        FeeBreakdown fees = feeCalculationService.calculate(
                product, performance.getUnitsSold(), performance.getRevenue());

        BigDecimal cogsTotal = MoneyMath.multiply(product.getCogs(), BigDecimal.valueOf(performance.getUnitsSold()));
        BigDecimal returnsCost = MoneyMath.multiply(performance.getRevenue(), performance.getReturnsRate());
        BigDecimal adSpend = MoneyMath.money(performance.getAdSpend());
        BigDecimal revenue = MoneyMath.money(performance.getRevenue());

        BigDecimal trueProfit = MoneyMath.money(
                revenue
                        .subtract(cogsTotal)
                        .subtract(fees.referralFee())
                        .subtract(fees.fbaFee())
                        .subtract(returnsCost)
                        .subtract(adSpend)
        );

        BigDecimal profitMargin = MoneyMath.divide(trueProfit, revenue);
        BigDecimal profitPerClick = MoneyMath.divide(trueProfit, performance.getClicks());
        BigDecimal acos = MoneyMath.divide(adSpend, revenue);
        BigDecimal averageDailyUnitsSold = trailingAverageUnitsSold(campaign.getId(), performance.getDate());

        return new ProfitabilityResponse(
                campaign.getAccountId(),
                campaign.getId(),
                campaign.getName(),
                campaign.getStatus(),
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                performance.getDate(),
                performance.getImpressions(),
                performance.getClicks(),
                performance.getUnitsSold(),
                product.getInventoryUnits(),
                averageDailyUnitsSold,
                campaign.getCurrentBid(),
                revenue,
                adSpend,
                cogsTotal,
                fees.referralFee(),
                fees.fbaFee(),
                returnsCost,
                trueProfit,
                profitMargin,
                profitPerClick,
                acos
        );
    }

    private BigDecimal trailingAverageUnitsSold(String campaignId, LocalDate date) {
        LocalDate windowStart = date.minusDays(VELOCITY_WINDOW_DAYS - 1L);
        List<DailyPerformance> window = dailyPerformanceRepository
                .findByCampaignIdAndDateBetween(campaignId, windowStart, date);
        if (window.isEmpty()) {
            return BigDecimal.ZERO.setScale(MoneyMath.RATIO_SCALE, MoneyMath.ROUNDING);
        }
        int totalUnits = window.stream().mapToInt(DailyPerformance::getUnitsSold).sum();
        return MoneyMath.divide(BigDecimal.valueOf(totalUnits), window.size());
    }

    private static MoneyTotals sum(List<ProfitabilityResponse> rows) {
        if (rows.isEmpty()) {
            return zeroTotals();
        }
        BigDecimal revenue = rows.stream()
                .map(ProfitabilityResponse::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal adSpend = rows.stream()
                .map(ProfitabilityResponse::adSpend)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal trueProfit = rows.stream()
                .map(ProfitabilityResponse::trueProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MoneyTotals(
                MoneyMath.money(revenue),
                MoneyMath.money(adSpend),
                MoneyMath.money(trueProfit),
                MoneyMath.divide(trueProfit, revenue),
                MoneyMath.divide(adSpend, revenue)
        );
    }

    private static MoneyTotals zeroTotals() {
        return new MoneyTotals(MoneyMath.money("0"), MoneyMath.money("0"), MoneyMath.money("0"), null, null);
    }
}
