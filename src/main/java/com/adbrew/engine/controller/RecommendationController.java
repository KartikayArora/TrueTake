package com.adbrew.engine.controller;

import com.adbrew.engine.dto.response.RecommendationResponse;
import com.adbrew.engine.dto.response.RecommendationRunResponse;
import com.adbrew.engine.seed.DemoIds;
import com.adbrew.engine.service.RecommendationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Rule engine that suggests PAUSE, LOWER_BID, or RAISE_BID from true profit.")
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;

    @PostMapping("/run/{accountId}")
    @Operation(
            summary = "Run recommendations",
            description = "Evaluates LowProfit, LowInventory, and HighPerformer rules for the account and stores the actions."
    )
    public RecommendationRunResponse run(
            @Parameter(
                    description = "Seeded demo account. Pick from the dropdown.",
                    schema = @Schema(
                            allowableValues = {DemoIds.ACC_LUMINA, DemoIds.ACC_NORTHSTAR},
                            example = DemoIds.ACC_LUMINA
                    )
            )
            @PathVariable String accountId,
            @Parameter(description = "Seeded performance day. Pick from the dropdown, or leave empty for yesterday.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return recommendationEngine.runForAccount(accountId, date);
    }

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "List recommendations",
            description = "Returns stored bid actions and the reasoning from the last run. Does not compute new ones."
    )
    public List<RecommendationResponse> list(
            @Parameter(
                    description = "Seeded demo account. Pick from the dropdown.",
                    schema = @Schema(
                            allowableValues = {DemoIds.ACC_LUMINA, DemoIds.ACC_NORTHSTAR},
                            example = DemoIds.ACC_LUMINA
                    )
            )
            @PathVariable String accountId) {
        return recommendationEngine.listForAccount(accountId);
    }
}
