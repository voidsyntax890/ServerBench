package com.serverbench.backend.dto.response;

import com.serverbench.engine.benchmark.ExperimentResult;

import java.util.List;

public class ExperimentResultResponse {

    private final String experimentId;
    private final String experimentName;
    private final int totalRuns;
    private final int successfulRuns;
    private final int failedRuns;
    private final List<ExperimentRunResponse> runs;

    public ExperimentResultResponse(
            ExperimentResult result
    ) {

        this.experimentId =
                result.getExperimentId();

        this.experimentName =
                result.getExperimentName();

        this.totalRuns =
                result.getRunCount();

        this.successfulRuns =
                result.getSuccessfulRunCount();

        this.failedRuns =
                result.getFailedRunCount();

        this.runs =
                result.getRunResults()
                        .stream()
                        .map(
                                ExperimentRunResponse::new
                        )
                        .toList();
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public int getSuccessfulRuns() {
        return successfulRuns;
    }

    public int getFailedRuns() {
        return failedRuns;
    }

    public List<ExperimentRunResponse> getRuns() {
        return runs;
    }
}