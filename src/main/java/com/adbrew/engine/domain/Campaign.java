package com.adbrew.engine.domain;

import com.adbrew.engine.domain.enums.CampaignStatus;
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
@Document(collection = "campaigns")
public class Campaign {

    @Id
    private String id;

    @Indexed
    @Field("accountId")
    private String accountId;

    @Indexed
    @Field("productId")
    private String productId;

    @Field("name")
    private String name;

    @Field("currentBid")
    private BigDecimal currentBid;

    @Field("status")
    private CampaignStatus status;
}
