package com.adbrew.engine.service;

import com.adbrew.engine.domain.DailyPerformance;
import com.adbrew.engine.dto.request.DailyPerformanceIngestItem;
import com.adbrew.engine.dto.response.IngestResponse;
import com.adbrew.engine.exception.ResourceNotFoundException;
import com.adbrew.engine.repository.CampaignRepository;
import com.adbrew.engine.repository.DailyPerformanceRepository;
import com.adbrew.engine.util.MoneyMath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestService {

    private final CampaignRepository campaignRepository;
    private final DailyPerformanceRepository dailyPerformanceRepository;

    public IngestResponse ingest(List<DailyPerformanceIngestItem> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("records must not be empty");
        }

        int inserted = 0;
        int updated = 0;
        for (DailyPerformanceIngestItem item : records) {
            campaignRepository.findById(item.campaignId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign", item.campaignId()));

            DailyPerformance existing = dailyPerformanceRepository
                    .findByCampaignIdAndDate(item.campaignId(), item.date())
                    .orElse(null);

            if (existing == null) {
                dailyPerformanceRepository.save(toNewDocument(item));
                inserted++;
            } else {
                apply(item, existing);
                dailyPerformanceRepository.save(existing);
                updated++;
            }
        }
        return new IngestResponse(inserted + updated, inserted, updated);
    }

    private static DailyPerformance toNewDocument(DailyPerformanceIngestItem item) {
        DailyPerformance performance = new DailyPerformance();
        apply(item, performance);
        return performance;
    }

    private static void apply(DailyPerformanceIngestItem item, DailyPerformance target) {
        target.setCampaignId(item.campaignId());
        target.setDate(item.date());
        target.setImpressions(item.impressions());
        target.setClicks(item.clicks());
        target.setAdSpend(MoneyMath.money(item.adSpend()));
        target.setUnitsSold(item.unitsSold());
        target.setRevenue(MoneyMath.money(item.revenue()));
        target.setReturnsRate(item.returnsRate());
    }
}
