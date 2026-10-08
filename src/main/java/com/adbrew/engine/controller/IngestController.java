package com.adbrew.engine.controller;

import com.adbrew.engine.dto.request.DailyPerformanceIngestItem;
import com.adbrew.engine.dto.response.IngestResponse;
import com.adbrew.engine.service.IngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
@Validated
@Tag(name = "Ingest", description = "Load daily ad performance so profitability and recommendations have data to use.")
public class IngestController {

    private final IngestService ingestService;

    @PostMapping("/performance")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Ingest daily performance",
            description = "Upserts impressions, clicks, spend, units, and revenue by campaign and date. Replaces seed rows when the same key is sent."
    )
    public IngestResponse ingest(@Valid @RequestBody List<@Valid DailyPerformanceIngestItem> records) {
        return ingestService.ingest(records);
    }
}
