package com.adbrew.engine.service;

import com.adbrew.engine.domain.Campaign;
import com.adbrew.engine.domain.DailyPerformance;
import com.adbrew.engine.domain.Recommendation;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import com.adbrew.engine.dto.response.RecommendationResponse;
import com.adbrew.engine.dto.response.RecommendationRunResponse;
import com.adbrew.engine.exception.ResourceNotFoundException;
import com.adbrew.engine.repository.AccountRepository;
import com.adbrew.engine.repository.CampaignRepository;
import com.adbrew.engine.repository.DailyPerformanceRepository;
import com.adbrew.engine.repository.RecommendationRepository;
import com.adbrew.engine.service.rule.OptimizationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationEngine {

    private final List<OptimizationRule> rules;
    private final AccountRepository accountRepository;
    private final CampaignRepository campaignRepository;
    private final DailyPerformanceRepository dailyPerformanceRepository;
    private final RecommendationRepository recommendationRepository;
    private final ProfitabilityService profitabilityService;

    public RecommendationRunResponse runForAccount(String accountId, LocalDate date) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        LocalDate requestedDate = resolveDate(date);
        List<Campaign> campaigns = campaignRepository.findByAccountId(accountId);
        List<String> campaignIds = campaigns.stream().map(Campaign::getId).toList();

        boolean anyPerformance = campaignIds.stream().anyMatch(id ->
                dailyPerformanceRepository.findByCampaignIdAndDate(id, requestedDate).isPresent());
        LocalDate latest = dailyPerformanceRepository.findTopByOrderByDateDesc()
                .map(DailyPerformance::getDate)
                .orElse(null);

        boolean fallbackToLatest = !anyPerformance && latest != null && !latest.equals(requestedDate);
        LocalDate targetDate = fallbackToLatest ? latest : requestedDate;
        String message;
        if (fallbackToLatest) {
            message = "No ad performance for " + requestedDate
                    + ". Seeded data ends on " + latest + ", so that day was evaluated instead.";
        } else if (!anyPerformance) {
            message = "No ad performance found for " + requestedDate + ".";
        } else {
            message = "Evaluated " + targetDate + ".";
        }

        if (!campaignIds.isEmpty()) {
            recommendationRepository.deleteByCampaignIdInAndDate(campaignIds, targetDate);
        }

        List<Recommendation> created = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            Optional<DailyPerformance> performance = dailyPerformanceRepository
                    .findByCampaignIdAndDate(campaign.getId(), targetDate);
            if (performance.isEmpty()) {
                continue;
            }

            ProfitabilityResponse snapshot = profitabilityService.compute(campaign, performance.get());
            for (OptimizationRule rule : rules) {
                rule.evaluate(snapshot).ifPresent(recommendation -> {
                    recommendation.setCreatedAt(Instant.now());
                    created.add(recommendationRepository.save(recommendation));
                });
            }
        }

        log.info("Recommendation engine produced {} actions for account {} on {}",
                created.size(), accountId, targetDate);

        List<RecommendationResponse> responses = created.stream().map(this::toResponse).toList();
        return new RecommendationRunResponse(accountId, targetDate, message, responses.size(), responses);
    }

    public List<RecommendationResponse> listForAccount(String accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        List<String> campaignIds = campaignRepository.findByAccountId(accountId).stream()
                .map(Campaign::getId)
                .toList();
        if (campaignIds.isEmpty()) {
            return List.of();
        }
        return recommendationRepository.findByCampaignIdInOrderByCreatedAtDesc(campaignIds).stream()
                .map(this::toResponse)
                .toList();
    }

    public LocalDate resolveDate(LocalDate date) {
        if (date != null) {
            return date;
        }
        return dailyPerformanceRepository.findTopByOrderByDateDesc()
                .map(DailyPerformance::getDate)
                .orElse(LocalDate.now().minusDays(1));
    }

    private RecommendationResponse toResponse(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.getId(),
                recommendation.getCampaignId(),
                recommendation.getDate(),
                recommendation.getRuleTriggered(),
                recommendation.getAction(),
                recommendation.getReasoning(),
                recommendation.getCreatedAt()
        );
    }
}
