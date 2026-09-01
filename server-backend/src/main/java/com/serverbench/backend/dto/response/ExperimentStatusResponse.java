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

    private final String currentArchitecture;
    private final int currentRepetition;

    private final int completedRuns;
    private final double progressPercentage;

    private final String errorMessage;

    public ExperimentStatusResponse(
            Experiment experiment,
            ExperimentService.ExperimentStatus status,
            ExperimentResult result,
            String errorMessage,
            com.serverbench.engine.benchmark.ExperimentProgress progress
    ) {

        this.id =
                experiment.getId();

        this.name =
                experiment.getName();

        this.status =
                status.name();

        if (result != null) {

            this.totalRuns =
                    result.getRunCount();

            this.successfulRuns =
                    result.getSuccessfulRunCount();

            this.failedRuns =
                    result.getFailedRunCount();

        } else if (progress != null) {

            this.totalRuns =
                    progress.getTotalRuns();

            this.successfulRuns =
                    0;

            this.failedRuns =
                    0;

        } else {

            this.totalRuns =
                    experiment.getArchitectures().size()
                            * experiment.getRepetitions();

            this.successfulRuns =
                    0;

            this.failedRuns =
                    0;
        }

        if (progress != null) {

            this.completedRuns =
                    progress.getCompletedRuns();

            this.currentArchitecture =
                    progress
                            .getCurrentArchitecture()
                            .name();

            this.currentRepetition =
                    progress.getCurrentRepetition();

            this.progressPercentage =
                    progress.getProgressPercentage();

        } else if (result != null) {

            this.completedRuns =
                    result.getRunCount();

            this.currentArchitecture =
                    null;

            this.currentRepetition =
                    0;

            this.progressPercentage =
                    100.0;

        } else {

            this.completedRuns =
                    0;

            this.currentArchitecture =
                    null;

            this.currentRepetition =
                    0;

            this.progressPercentage =
                    0.0;
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

    public int getCompletedRuns() {
        return completedRuns;
    }

    public String getCurrentArchitecture() {
        return currentArchitecture;
    }

    public int getCurrentRepetition() {
        return currentRepetition;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}