package com.adbrew.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "daily_performance")
@CompoundIndex(name = "campaign_date_idx", def = "{'campaignId': 1, 'date': 1}", unique = true)
public class DailyPerformance {

    @Id
    private String id;

    @Field("campaignId")
    private String campaignId;

    @Field("date")
    private LocalDate date;

    @Field("impressions")
    private int impressions;

    @Field("clicks")
    private int clicks;

    @Field("adSpend")
    private BigDecimal adSpend;

    @Field("unitsSold")
    private int unitsSold;

    @Field("revenue")
    private BigDecimal revenue;

    /** Fraction of revenue lost to returns, e.g. 0.05 for 5%. */
    @Field("returnsRate")
    private BigDecimal returnsRate;
}
