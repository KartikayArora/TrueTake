package com.adbrew.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * One FBA fulfillment-fee band: shipping weight up to {@code maxOz} (inclusive)
 * is charged {@code fee} per unit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FbaWeightTier {

    @Field("maxOz")
    private int maxOz;

    @Field("fee")
    private BigDecimal fee;
}
