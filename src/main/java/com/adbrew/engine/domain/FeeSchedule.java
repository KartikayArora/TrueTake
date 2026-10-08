package com.adbrew.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "fee_schedules")
public class FeeSchedule {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("category")
    private String category;

    /**
     * Amazon referral fee as a decimal fraction of item revenue (e.g. 0.15 = 15%).
     * Seeded from published US Seller Central category rates.
     */
    @Field("referralFeePercent")
    private BigDecimal referralFeePercent;

    /**
     * FBA pick/pack/ship fee bands ordered by ascending max shipping weight (oz).
     * Lookup uses the first band whose maxOz is greater than or equal to the product weight.
     */
    @Field("fbaFeeByWeightTier")
    private List<FbaWeightTier> fbaFeeByWeightTier;
}
