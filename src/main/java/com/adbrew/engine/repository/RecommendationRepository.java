package com.adbrew.engine.repository;

import com.adbrew.engine.domain.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {

    List<Recommendation> findByCampaignIdInOrderByCreatedAtDesc(Collection<String> campaignIds);

    void deleteByCampaignIdInAndDate(Collection<String> campaignIds, LocalDate date);
}
