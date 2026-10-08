package com.adbrew.engine.domain;

import com.adbrew.engine.domain.enums.RecommendationAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "recommendations")
@CompoundIndex(name = "campaign_date_rule_idx", def = "{'campaignId': 1, 'date': 1, 'ruleTriggered': 1}")
public class Recommendation {

    @Id
    private String id;

    @Indexed
    @Field("campaignId")
    private String campaignId;

    @Field("date")
    private LocalDate date;

    @Field("ruleTriggered")
    private String ruleTriggered;

    @Field("action")
    private RecommendationAction action;

    @Field("reasoning")
    private String reasoning;

    @Field("createdAt")
    private Instant createdAt;
}
