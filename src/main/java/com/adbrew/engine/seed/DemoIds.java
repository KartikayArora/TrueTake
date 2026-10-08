package com.adbrew.engine.seed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stable IDs written by {@link DataSeeder} and advertised in Swagger as dropdowns.
 * The API still accepts any string; these are the seeded demo values.
 */
public final class DemoIds {

    /** Matches {@link DataSeeder}: 28 days of ads ending yesterday. */
    public static final int PERFORMANCE_DAYS = 28;

    public static final String ACC_LUMINA = "acc-lumina";
    public static final String ACC_NORTHSTAR = "acc-northstar";

    public static final String CAMP_SERUM = "camp-serum";
    public static final String CAMP_MOISTURIZER = "camp-moisturizer";
    public static final String CAMP_RETINOL = "camp-retinol";
    public static final String CAMP_COLLAGEN = "camp-collagen";
    public static final String CAMP_TONER = "camp-toner";
    public static final String CAMP_MAGNESIUM = "camp-magnesium";
    public static final String CAMP_OMEGA = "camp-omega";
    public static final String CAMP_CUTTING_BOARD = "camp-cutting-board";
    public static final String CAMP_SHEETS = "camp-sheets";
    public static final String CAMP_DIFFUSER = "camp-diffuser";

    public static final List<String> ACCOUNT_IDS = List.of(ACC_LUMINA, ACC_NORTHSTAR);

    public static final List<String> CAMPAIGN_IDS = List.of(
            CAMP_SERUM,
            CAMP_MOISTURIZER,
            CAMP_RETINOL,
            CAMP_COLLAGEN,
            CAMP_TONER,
            CAMP_MAGNESIUM,
            CAMP_OMEGA,
            CAMP_CUTTING_BOARD,
            CAMP_SHEETS,
            CAMP_DIFFUSER
    );

    public static final String PROD_SERUM = "prod-serum";
    public static final String PROD_MOISTURIZER = "prod-moisturizer";
    public static final String PROD_RETINOL = "prod-retinol";
    public static final String PROD_COLLAGEN = "prod-collagen";
    public static final String PROD_TONER = "prod-toner";
    public static final String PROD_MAGNESIUM = "prod-magnesium";
    public static final String PROD_OMEGA = "prod-omega";
    public static final String PROD_CUTTING_BOARD = "prod-cutting-board";
    public static final String PROD_SHEETS = "prod-sheets";
    public static final String PROD_DIFFUSER = "prod-diffuser";

    /** Newest seeded day first, so Swagger's date dropdown starts on yesterday. */
    public static List<String> seededPerformanceDates() {
        LocalDate end = LocalDate.now().minusDays(1);
        List<String> dates = new ArrayList<>(PERFORMANCE_DAYS);
        for (int i = 0; i < PERFORMANCE_DAYS; i++) {
            dates.add(end.minusDays(i).toString());
        }
        return dates;
    }

    public static String latestSeededDate() {
        return LocalDate.now().minusDays(1).toString();
    }

    private DemoIds() {
    }
}
