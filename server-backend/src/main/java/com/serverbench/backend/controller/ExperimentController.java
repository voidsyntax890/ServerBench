package com.serverbench.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverbench.backend.dto.request.ExperimentRequest;
import com.serverbench.backend.dto.response.ComparisonResponse;
import com.serverbench.backend.dto.response.ExperimentHistoryResponse;
import com.serverbench.backend.dto.response.ExperimentResponse;
import com.serverbench.backend.dto.response.ExperimentResultResponse;
import com.serverbench.backend.dto.response.ExperimentStartResponse;
import com.serverbench.backend.dto.response.ExperimentStatusResponse;
import com.serverbench.backend.service.ExperimentService;
import com.serverbench.engine.benchmark.ComparisonSummary;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ExperimentResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/experiments")
@CrossOrigin(
        origins = "http://localhost:5173"
)
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(
            ExperimentService experimentService
    ) {
        this.experimentService =
                experimentService;
    }

    // ================================================================
    // CREATE EXPERIMENT
    // ================================================================

    @PostMapping
    public ResponseEntity<ExperimentResponse> createExperiment(
            @Valid @RequestBody ExperimentRequest request
    ) {

        Experiment experiment =
                experimentService.createExperiment(
                        request
                );

        ExperimentResponse response =
                new ExperimentResponse(
                        experiment,
                        request.getThreadPoolSize(),
                        experimentService
                                .getStatus(
                                        experiment.getId()
                                )
                                .name()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ================================================================
    // GET EXPERIMENT HISTORY
    // ================================================================

    @GetMapping
    public ResponseEntity<ExperimentHistoryResponse>
    getExperimentHistory() {

        List<Experiment> experiments =
                experimentService.getAllExperiments();

        List<ExperimentResponse> responses =
                experiments.stream()
                        .map(
                                experiment ->
                                        new ExperimentResponse(
                                                experiment,
                                                experimentService
                                                        .getThreadPoolSize(
                                                                experiment.getId()
                                                        ),
                                                experimentService
                                                        .getStatus(
                                                                experiment.getId()
                                                        )
                                                        .name()
                                        )
                        )
                        .toList();

        ExperimentHistoryResponse response =
                new ExperimentHistoryResponse(
                        responses
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ================================================================
    // GET EXPERIMENT DETAILS
    // ================================================================

    @GetMapping("/{experimentId}/details")
    public ResponseEntity<ExperimentResponse>
    getExperimentDetails(
            @PathVariable("experimentId")
            String experimentId
    ) {

        Experiment experiment =
                experimentService.getExperiment(
                        experimentId
                );

        ExperimentResponse response =
                new ExperimentResponse(
                        experiment,
                        experimentService
                                .getThreadPoolSize(
                                        experimentId
                                ),
                        experimentService
                                .getStatus(
                                        experimentId
                                )
                                .name()
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ================================================================
    // START EXPERIMENT
    // ================================================================

    @PostMapping("/{experimentId}/start")
    public ResponseEntity<ExperimentStartResponse>
    startExperiment(
            @PathVariable("experimentId")
            String experimentId
    ) {

        experimentService.startExperiment(
                experimentId
        );

        ExperimentStartResponse response =
                new ExperimentStartResponse(
                        experimentId,
                        experimentService
                                .getStatus(
                                        experimentId
                                )
                                .name(),
                        "Experiment started successfully."
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // ================================================================
    // GET EXPERIMENT STATUS
    // ================================================================

    @GetMapping("/{experimentId}")
    public ResponseEntity<ExperimentStatusResponse>
    getExperiment(
            @PathVariable("experimentId")
            String experimentId
    ) {

        Experiment experiment =
                experimentService.getExperiment(
                        experimentId
                );

        ExperimentService.ExperimentStatus status =
                experimentService.getStatus(
                        experimentId
                );

        ExperimentResult result =
                experimentService.getResult(
                        experimentId
                );

        String errorMessage =
                experimentService.getErrorMessage(
                        experimentId
                );

        ExperimentStatusResponse response =
                new ExperimentStatusResponse(
                        experiment,
                        status,
                        result,
                        errorMessage,
                        experimentService
                                .getProgress(
                                        experimentId
                                )
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ================================================================
    // GET EXPERIMENT RESULTS
    // ================================================================

    @GetMapping("/{experimentId}/results")
    public ResponseEntity<ExperimentResultResponse>
    getExperimentResults(
            @PathVariable("experimentId")
            String experimentId
    ) {

        ExperimentResult result =
                experimentService.getResult(
                        experimentId
                );

        if (result == null) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        ExperimentResultResponse response =
                new ExperimentResultResponse(
                        result
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ================================================================
    // GET EXPERIMENT COMPARISON
    // ================================================================

    @GetMapping("/{experimentId}/comparison")
    public ResponseEntity<ComparisonResponse>
    getExperimentComparison(
            @PathVariable("experimentId")
            String experimentId
    ) {

        ComparisonSummary summary =
                experimentService.getComparisonSummary(
                        experimentId
                );

        ComparisonResponse response =
                new ComparisonResponse(
                        summary
                );

        return ResponseEntity.ok(
                response
        );
    }
}