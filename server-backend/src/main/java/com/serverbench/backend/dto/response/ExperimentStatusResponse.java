package com.serverbench.backend.dto.response;

import com.serverbench.backend.service.ExperimentService;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ExperimentResult;

public class ExperimentStatusResponse {

    private final String id;
    private final String name;
    private final String status;

    private final int totalRuns;
    private final int successfulRuns;
    private final int failedRuns;

    private final String errorMessage;

    public ExperimentStatusResponse(
            Experiment experiment,
            ExperimentService.ExperimentStatus status,
            ExperimentResult result,
            String errorMessage
    ) {

        this.id =
                experiment.getId();

        this.name =
                experiment.getName();

        this.status =
                status.name();

        if (result == null) {

            this.totalRuns = 0;
            this.successfulRuns = 0;
            this.failedRuns = 0;

        } else {

            this.totalRuns =
                    result.getRunCount();

            this.successfulRuns =
                    result.getSuccessfulRunCount();

            this.failedRuns =
                    result.getFailedRunCount();
        }

        this.errorMessage =
                errorMessage == null
                        ? ""
                        : errorMessage;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
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

    public String getErrorMessage() {
        return errorMessage;
    }
}