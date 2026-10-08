package com.adbrew.engine.controller;

import com.adbrew.engine.dto.response.AccountProfitabilitySummary;
import com.adbrew.engine.dto.response.ProfitabilityResponse;
import com.adbrew.engine.seed.DemoIds;
import com.adbrew.engine.service.ProfitabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/profitability")
@RequiredArgsConstructor
@Tag(name = "Profitability", description = "True profit after COGS, Amazon referral fees, FBA, returns, and ad spend.")
public class ProfitabilityController {

    private final ProfitabilityService profitabilityService;

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "Account profitability",
            description = "Rolls up true profit, margin, and ACOS for every campaign in the account over a date range."
    )
    public AccountProfitabilitySummary account(
            @Parameter(
                    description = "Seeded demo account. Pick from the dropdown.",
                    schema = @Schema(
                            allowableValues = {DemoIds.ACC_LUMINA, DemoIds.ACC_NORTHSTAR},
                            example = DemoIds.ACC_LUMINA
                    )
            )
            @PathVariable String accountId,
            @Parameter(description = "Inclusive start. Pick a seeded day from the dropdown.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @Parameter(description = "Inclusive end. Pick a seeded day from the dropdown.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {
        return profitabilityService.forAccount(accountId, from, to);
    }

    @GetMapping("/campaign/{campaignId}")
    @Operation(
            summary = "Campaign profitability",
            description = "Computes one campaign's true profit for a day: revenue minus COGS, fees, returns, and ad spend."
    )
    public ProfitabilityResponse campaign(
            @Parameter(
                    description = "Seeded demo campaign. Pick from the dropdown.",
                    schema = @Schema(
                            allowableValues = {
                                    DemoIds.CAMP_SERUM,
                                    DemoIds.CAMP_MOISTURIZER,
                                    DemoIds.CAMP_RETINOL,
                                    DemoIds.CAMP_COLLAGEN,
                                    DemoIds.CAMP_TONER,
                                    DemoIds.CAMP_MAGNESIUM,
                                    DemoIds.CAMP_OMEGA,
                                    DemoIds.CAMP_CUTTING_BOARD,
                                    DemoIds.CAMP_SHEETS,
                                    DemoIds.CAMP_DIFFUSER
                            },
                            example = DemoIds.CAMP_SERUM
                    )
            )
            @PathVariable String campaignId,
            @Parameter(description = "Seeded performance day. Pick from the dropdown, or leave empty for yesterday.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return profitabilityService.forCampaignOnDate(campaignId, date);
    }
}
