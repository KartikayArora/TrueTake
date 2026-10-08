package com.adbrew.engine.repository;

import com.adbrew.engine.domain.DailyPerformance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DailyPerformanceRepository extends MongoRepository<DailyPerformance, String> {

    Optional<DailyPerformance> findByCampaignIdAndDate(String campaignId, LocalDate date);

    List<DailyPerformance> findByCampaignIdAndDateBetween(String campaignId, LocalDate from, LocalDate to);

    List<DailyPerformance> findByCampaignIdInAndDateBetween(
            Collection<String> campaignIds, LocalDate from, LocalDate to);

    Optional<DailyPerformance> findTopByOrderByDateDesc();

    Optional<DailyPerformance> findTopByCampaignIdOrderByDateDesc(String campaignId);
}
