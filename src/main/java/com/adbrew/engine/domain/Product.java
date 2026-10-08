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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed
    @Field("accountId")
    private String accountId;

    @Indexed
    @Field("sku")
    private String sku;

    @Field("name")
    private String name;

    @Field("cogs")
    private BigDecimal cogs;

    @Field("weightTierOz")
    private int weightTierOz;

    @Field("inventoryUnits")
    private int inventoryUnits;

    /** Amazon fee category, used to resolve {@link FeeSchedule}. */
    @Indexed
    @Field("category")
    private String category;
}
