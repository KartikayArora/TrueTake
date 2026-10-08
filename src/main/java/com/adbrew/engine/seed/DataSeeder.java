package com.adbrew.engine.seed;

import com.adbrew.engine.domain.Account;
import com.adbrew.engine.domain.Campaign;
import com.adbrew.engine.domain.DailyPerformance;
import com.adbrew.engine.domain.FbaWeightTier;
import com.adbrew.engine.domain.FeeSchedule;
import com.adbrew.engine.domain.Product;
import com.adbrew.engine.domain.enums.AccountType;
import com.adbrew.engine.domain.enums.CampaignStatus;
import com.adbrew.engine.repository.AccountRepository;
import com.adbrew.engine.repository.CampaignRepository;
import com.adbrew.engine.repository.DailyPerformanceRepository;
import com.adbrew.engine.repository.FeeScheduleRepository;
import com.adbrew.engine.repository.ProductRepository;
import com.adbrew.engine.util.MoneyMath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.adbrew.engine.seed.DemoIds.ACC_LUMINA;
import static com.adbrew.engine.seed.DemoIds.ACC_NORTHSTAR;
import static com.adbrew.engine.seed.DemoIds.CAMP_COLLAGEN;
import static com.adbrew.engine.seed.DemoIds.CAMP_CUTTING_BOARD;
import static com.adbrew.engine.seed.DemoIds.CAMP_DIFFUSER;
import static com.adbrew.engine.seed.DemoIds.CAMP_MAGNESIUM;
import static com.adbrew.engine.seed.DemoIds.CAMP_MOISTURIZER;
import static com.adbrew.engine.seed.DemoIds.CAMP_OMEGA;
import static com.adbrew.engine.seed.DemoIds.CAMP_RETINOL;
import static com.adbrew.engine.seed.DemoIds.CAMP_SERUM;
import static com.adbrew.engine.seed.DemoIds.CAMP_SHEETS;
import static com.adbrew.engine.seed.DemoIds.CAMP_TONER;
import static com.adbrew.engine.seed.DemoIds.PROD_COLLAGEN;
import static com.adbrew.engine.seed.DemoIds.PROD_CUTTING_BOARD;
import static com.adbrew.engine.seed.DemoIds.PROD_DIFFUSER;
import static com.adbrew.engine.seed.DemoIds.PROD_MAGNESIUM;
import static com.adbrew.engine.seed.DemoIds.PROD_MOISTURIZER;
import static com.adbrew.engine.seed.DemoIds.PROD_OMEGA;
import static com.adbrew.engine.seed.DemoIds.PROD_RETINOL;
import static com.adbrew.engine.seed.DemoIds.PROD_SERUM;
import static com.adbrew.engine.seed.DemoIds.PROD_SHEETS;
import static com.adbrew.engine.seed.DemoIds.PROD_TONER;

/**
 * Loads a demo catalog the first time the app starts against an empty database.
 *
 * <p>Ad performance and product COGS/inventory are synthetic. Fee schedules are
 * based on Amazon's published US referral-fee category rates and 2026 FBA
 * fulfillment fees (non-peak, $10 to $50 selling-price band).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final CampaignRepository campaignRepository;
    private final DailyPerformanceRepository dailyPerformanceRepository;
    private final FeeScheduleRepository feeScheduleRepository;

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            log.info("Seed skipped: accounts collection already has data");
            return;
        }

        seedAccounts();
        seedFeeSchedules();
        seedProductsAndCampaigns();
        seedPerformance();
        log.info("Seed complete: 2 accounts, {} products, {} campaigns, {} performance days ending {}",
                productRepository.count(),
                campaignRepository.count(),
                DemoIds.PERFORMANCE_DAYS,
                LocalDate.now().minusDays(1));
    }

    private void seedAccounts() {
        accountRepository.saveAll(List.of(
                Account.builder()
                        .id(ACC_LUMINA)
                        .name("Lumina Beauty Co")
                        .type(AccountType.BRAND)
                        .build(),
                Account.builder()
                        .id(ACC_NORTHSTAR)
                        .name("Northstar Retail Media")
                        .type(AccountType.AGENCY)
                        .build()
        ));
    }

    /**
     * Referral fees: US Amazon Seller Central category rates (2026). Beauty / Health
     * &amp; Personal Care are 8% at or below $10 and 15% above $10; every seeded SKU
     * is priced above $10 so we store 15%. Home &amp; Kitchen is a flat 15%.
     *
     * <p>FBA fees: US small-standard and large-standard non-peak fulfillment rates
     * effective 15 Jan 2026, $10–$50 price band (the band these SKUs fall into).
     * Does not include the 3.5% fuel/logistics surcharge introduced 17 Apr 2026.
     *
     * @see <a href="https://sellercentral.amazon.com/help/hub/reference/G1791">Amazon referral fees</a>
     * @see <a href="https://sellercentral.amazon.com/help/hub/reference/G201812170">Amazon FBA fulfillment fees</a>
     */
    private void seedFeeSchedules() {
        List<FbaWeightTier> fbaTiers = List.of(
                tier(2, "3.32"),
                tier(4, "3.42"),
                tier(6, "3.45"),
                tier(8, "3.54"),
                tier(10, "3.68"),
                tier(12, "3.78"),
                tier(14, "3.91"),
                tier(16, "3.96"),
                tier(20, "5.04"),
                tier(24, "5.42"),
                tier(28, "5.57"),
                tier(32, "5.82"),
                tier(36, "5.92"),
                tier(40, "6.10"),
                tier(44, "6.26"),
                tier(48, "6.67"),
                tier(80, "7.29")
        );

        feeScheduleRepository.saveAll(List.of(
                FeeSchedule.builder()
                        .id("fee-beauty")
                        .category("Beauty & Personal Care")
                        .referralFeePercent(new BigDecimal("0.15"))
                        .fbaFeeByWeightTier(fbaTiers)
                        .build(),
                FeeSchedule.builder()
                        .id("fee-health")
                        .category("Health & Personal Care")
                        .referralFeePercent(new BigDecimal("0.15"))
                        .fbaFeeByWeightTier(fbaTiers)
                        .build(),
                FeeSchedule.builder()
                        .id("fee-home")
                        .category("Home & Kitchen")
                        .referralFeePercent(new BigDecimal("0.15"))
                        .fbaFeeByWeightTier(fbaTiers)
                        .build()
        ));
    }

    private void seedProductsAndCampaigns() {
        List<Product> products = List.of(
                product(PROD_SERUM, ACC_LUMINA, "LUM-SERUM-30", "Vitamin C Brightening Serum 30ml",
                        "4.20", 4, 420, "Beauty & Personal Care"),
                product(PROD_MOISTURIZER, ACC_LUMINA, "LUM-MOIST-50", "Hyaluronic Daily Moisturizer",
                        "5.50", 6, 320, "Beauty & Personal Care"),
                product(PROD_RETINOL, ACC_LUMINA, "LUM-RET-30", "Retinol Night Cream 30ml",
                        "8.80", 5, 280, "Beauty & Personal Care"),
                product(PROD_COLLAGEN, ACC_LUMINA, "LUM-COL-60", "Collagen Gummies 60ct",
                        "6.00", 12, 600, "Health & Personal Care"),
                product(PROD_TONER, ACC_LUMINA, "LUM-TON-100", "Niacinamide Toner 100ml",
                        "3.10", 8, 15, "Beauty & Personal Care"),
                product(PROD_MAGNESIUM, ACC_NORTHSTAR, "NS-MAG-120", "Magnesium Glycinate 120ct",
                        "7.50", 10, 1200, "Health & Personal Care"),
                product(PROD_OMEGA, ACC_NORTHSTAR, "NS-OMG-90", "Omega-3 Softgels 90ct",
                        "9.20", 8, 35, "Health & Personal Care"),
                product(PROD_CUTTING_BOARD, ACC_NORTHSTAR, "NS-CUT-BAM", "Bamboo Cutting Board",
                        "8.00", 24, 180, "Home & Kitchen"),
                product(PROD_SHEETS, ACC_NORTHSTAR, "NS-SHT-QUEEN", "Microfiber Queen Sheet Set",
                        "12.50", 48, 220, "Home & Kitchen"),
                product(PROD_DIFFUSER, ACC_NORTHSTAR, "NS-DIF-CER", "Ceramic Essential Oil Diffuser",
                        "6.80", 16, 210, "Home & Kitchen")
        );
        productRepository.saveAll(products);

        campaignRepository.saveAll(List.of(
                campaign(CAMP_SERUM, ACC_LUMINA, PROD_SERUM, "SP - Vitamin C Serum", "1.15"),
                campaign(CAMP_MOISTURIZER, ACC_LUMINA, PROD_MOISTURIZER, "SP - Hyaluronic Moisturizer", "0.95"),
                campaign(CAMP_RETINOL, ACC_LUMINA, PROD_RETINOL, "SP - Retinol Night Cream", "1.40"),
                campaign(CAMP_COLLAGEN, ACC_LUMINA, PROD_COLLAGEN, "SP - Collagen Gummies", "1.80"),
                campaign(CAMP_TONER, ACC_LUMINA, PROD_TONER, "SP - Niacinamide Toner", "0.70"),
                campaign(CAMP_MAGNESIUM, ACC_NORTHSTAR, PROD_MAGNESIUM, "SP - Magnesium Glycinate", "1.05"),
                campaign(CAMP_OMEGA, ACC_NORTHSTAR, PROD_OMEGA, "SP - Omega-3 Softgels", "1.25"),
                campaign(CAMP_CUTTING_BOARD, ACC_NORTHSTAR, PROD_CUTTING_BOARD, "SP - Bamboo Cutting Board", "1.60"),
                campaign(CAMP_SHEETS, ACC_NORTHSTAR, PROD_SHEETS, "SP - Queen Sheet Set", "1.90"),
                campaign(CAMP_DIFFUSER, ACC_NORTHSTAR, PROD_DIFFUSER, "SP - Ceramic Diffuser", "1.10")
        ));
    }

    private void seedPerformance() {
        LocalDate end = LocalDate.now().minusDays(1);
        Random random = new Random(42);
        List<DailyPerformance> rows = new ArrayList<>();

        // High performer: strong contribution after fees, modest spend.
        rows.addAll(series(CAMP_SERUM, end, random, 9000, 0.04, "0.85", 0.12, "24.99", "0.04", 0.80));
        rows.addAll(series(CAMP_MOISTURIZER, end, random, 7000, 0.035, "0.75", 0.10, "22.00", "0.05", 1.05));
        rows.addAll(series(CAMP_RETINOL, end, random, 5000, 0.03, "1.10", 0.09, "34.99", "0.04", 1.10));
        // Thin margin + overspend. LowProfitRule should fire.
        rows.addAll(series(CAMP_COLLAGEN, end, random, 12000, 0.05, "1.55", 0.06, "19.99", "0.08", 1.85));
        // Healthy unit economics but inventory is 15 units. LowInventoryRule.
        rows.addAll(series(CAMP_TONER, end, random, 4000, 0.045, "0.55", 0.14, "16.99", "0.05", 0.90));
        // Clear high performer. HighPerformerRule.
        rows.addAll(series(CAMP_MAGNESIUM, end, random, 11000, 0.045, "0.70", 0.16, "27.99", "0.03", 0.75));
        // Decent profit, low inventory (35 units).
        rows.addAll(series(CAMP_OMEGA, end, random, 6000, 0.04, "0.95", 0.10, "24.99", "0.04", 1.00));
        // Unprofitable after heavy FBA + high CPC.
        rows.addAll(series(CAMP_CUTTING_BOARD, end, random, 8000, 0.03, "1.70", 0.07, "21.99", "0.06", 1.70));
        rows.addAll(series(CAMP_SHEETS, end, random, 5000, 0.025, "2.10", 0.06, "39.99", "0.10", 1.80));
        rows.addAll(series(CAMP_DIFFUSER, end, random, 6500, 0.035, "0.90", 0.11, "29.99", "0.05", 1.05));

        dailyPerformanceRepository.saveAll(rows);
    }

    /**
     * Builds {@value DemoIds#PERFORMANCE_DAYS} days of slightly noisy but directionally stable metrics.
     * {@code spendPressure} &gt; 1 inflates CPC-driven spend relative to conversion so the SKU
     * can be pushed underwater after fees.
     */
    private List<DailyPerformance> series(
            String campaignId,
            LocalDate end,
            Random random,
            int baseImpressions,
            double ctr,
            String cpc,
            double conversionRate,
            String aov,
            String returnsRate,
            double spendPressure) {

        List<DailyPerformance> days = new ArrayList<>();
        BigDecimal cpcValue = new BigDecimal(cpc);
        BigDecimal aovValue = new BigDecimal(aov);
        BigDecimal returns = new BigDecimal(returnsRate);

        for (int i = DemoIds.PERFORMANCE_DAYS - 1; i >= 0; i--) {
            double jitter = 0.82 + random.nextDouble() * 0.36;
            int impressions = Math.max(200, (int) Math.round(baseImpressions * jitter));
            int clicks = Math.max(5, (int) Math.round(impressions * ctr * (0.9 + random.nextDouble() * 0.2)));
            BigDecimal adSpend = MoneyMath.money(
                    cpcValue
                            .multiply(BigDecimal.valueOf(clicks))
                            .multiply(BigDecimal.valueOf(spendPressure))
                            .multiply(BigDecimal.valueOf(0.92 + random.nextDouble() * 0.16)));
            int unitsSold = Math.max(1, (int) Math.round(clicks * conversionRate * (0.9 + random.nextDouble() * 0.2)));
            BigDecimal revenue = MoneyMath.money(aovValue.multiply(BigDecimal.valueOf(unitsSold)));

            days.add(DailyPerformance.builder()
                    .campaignId(campaignId)
                    .date(end.minusDays(i))
                    .impressions(impressions)
                    .clicks(clicks)
                    .adSpend(adSpend)
                    .unitsSold(unitsSold)
                    .revenue(revenue)
                    .returnsRate(returns.setScale(4, RoundingMode.HALF_UP))
                    .build());
        }
        return days;
    }

    private static Product product(
            String id, String accountId, String sku, String name, String cogs,
            int weightOz, int inventory, String category) {
        return Product.builder()
                .id(id)
                .accountId(accountId)
                .sku(sku)
                .name(name)
                .cogs(MoneyMath.money(cogs))
                .weightTierOz(weightOz)
                .inventoryUnits(inventory)
                .category(category)
                .build();
    }

    private static Campaign campaign(String id, String accountId, String productId, String name, String bid) {
        return Campaign.builder()
                .id(id)
                .accountId(accountId)
                .productId(productId)
                .name(name)
                .currentBid(MoneyMath.money(bid))
                .status(CampaignStatus.ACTIVE)
                .build();
    }

    private static FbaWeightTier tier(int maxOz, String fee) {
        return FbaWeightTier.builder()
                .maxOz(maxOz)
                .fee(MoneyMath.money(fee))
                .build();
    }
}
