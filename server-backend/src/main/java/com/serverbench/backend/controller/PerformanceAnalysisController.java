package com.serverbench.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.serverbench.backend.dto.request.PerformanceExplanationRequest;
import com.serverbench.backend.dto.response.PerformanceExplanationResponse;
import com.serverbench.backend.service.ExperimentService;
import com.serverbench.backend.service.PerformanceExplanationService;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.analysis.BottleneckFinding;
import com.serverbench.engine.benchmark.analysis.BottleneckReport;
import com.serverbench.engine.benchmark.analysis.RuleBasedBottleneckAnalyzer;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/experiments")
@CrossOrigin(
        origins = "http://localhost:5173"
)
public class PerformanceAnalysisController {

    private final ExperimentService experimentService;
    private final PerformanceExplanationService
            performanceExplanationService;

    public PerformanceAnalysisController(
            ExperimentService experimentService,
            PerformanceExplanationService performanceExplanationService
    ) {
        this.experimentService =
                experimentService;

        this.performanceExplanationService =
                performanceExplanationService;
    }

    @PostMapping("/{experimentId}/analysis/explanation")
    public ResponseEntity<PerformanceExplanationResponse>
    explainFinding(
            @PathVariable("experimentId")
            String experimentId,

            @Valid
            @RequestBody
            PerformanceExplanationRequest request
    ) {

        ExperimentResult experimentResult =
                experimentService.getResult(
                        experimentId
                );

        if (experimentResult == null) {
            throw new IllegalStateException(
                    "Experiment results are not available yet."
            );
        }

        BottleneckReport report =
                new RuleBasedBottleneckAnalyzer()
                        .analyze(experimentResult);

        int findingIndex =
                request.getFindingIndex();

        if (findingIndex >= report.getFindings().size()) {
            throw new IllegalArgumentException(
                    "Finding index is outside the available analysis findings."
            );
        }

        BottleneckFinding finding =
                report.getFindings()
                        .get(findingIndex);

        String explanation =
                performanceExplanationService
                        .explain(finding);

        PerformanceExplanationResponse response =
                new PerformanceExplanationResponse(
                        experimentId,
                        findingIndex,
                        finding.getTitle(),
                        explanation
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}