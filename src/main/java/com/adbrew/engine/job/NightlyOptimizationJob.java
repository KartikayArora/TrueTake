package com.adbrew.engine.job;

import com.adbrew.engine.domain.Account;
import com.adbrew.engine.repository.AccountRepository;
import com.adbrew.engine.service.RecommendationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NightlyOptimizationJob {

    private final AccountRepository accountRepository;
    private final RecommendationEngine recommendationEngine;

    @Scheduled(cron = "${adbrew.optimization.cron}")
    public void run() {
        log.info("Nightly optimization job starting");
        for (Account account : accountRepository.findAll()) {
            recommendationEngine.runForAccount(account.getId(), recommendationEngine.resolveDate(null));
        }
        log.info("Nightly optimization job finished");
    }
}
