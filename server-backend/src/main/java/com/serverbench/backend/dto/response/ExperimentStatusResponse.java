package com.serverbench.backend.dto.response;

import com.serverbench.backend.service.ExperimentService;
import com.serverbench.engine.benchmark.BenchmarkMetricsSnapshot;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ExperimentProgress;
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

    // ================================================================
    // LIVE BENCHMARK METRICS
    // ================================================================

    private final int currentAttemptedRequests;
    private final int currentSuccessfulRequests;
    private final int currentFailedRequests;

    private final double currentThroughputRequestsPerSecond;
    private final double currentAverageLatencyMs;

    private final long currentElapsedTimeMs;

    /*
     * Backward-compatible constructor.
     *
     * Existing callers can continue using the M7.1 constructor.
     * Live metrics will simply be empty until the new constructor
     * is used.
     */
    public ExperimentStatusResponse(
            Experiment experiment,
            ExperimentService.ExperimentStatus status,
            ExperimentResult result,
            String errorMessage,
            ExperimentProgress progress
    ) {

        this(
                experiment,
                status,
                result,
                errorMessage,
                progress,
                null
        );
    }

    /*
     * M7.3 constructor.
     */
    public ExperimentStatusResponse(
            Experiment experiment,
            ExperimentService.ExperimentStatus status,
            ExperimentResult result,
            String errorMessage,
            ExperimentProgress progress,
            BenchmarkMetricsSnapshot liveMetrics
    ) {

        this.id =
                experiment.getId();

        this.name =
                experiment.getName();

        this.status =
                status.name();

        // ============================================================
        // EXPERIMENT RUN COUNTS
        // ============================================================

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

        // ============================================================
        // EXPERIMENT PROGRESS
        // ============================================================

        if (progress != null) {

            this.completedRuns =
                    progress.getCompletedRuns();

            this.currentArchitecture =
                    progress.getCurrentArchitecture();

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

        // ============================================================
        // LIVE BENCHMARK METRICS
        // ============================================================

        if (liveMetrics != null) {

            this.currentAttemptedRequests =
                    liveMetrics.getAttemptedRequests();

            this.currentSuccessfulRequests =
                    liveMetrics.getSuccessfulRequests();

            this.currentFailedRequests =
                    liveMetrics.getFailedRequests();

            this.currentThroughputRequestsPerSecond =
                    liveMetrics
                            .getThroughputRequestsPerSecond();

            this.currentAverageLatencyMs =
                    liveMetrics
                            .getAverageLatencyMs();

            this.currentElapsedTimeMs =
                    liveMetrics.getElapsedTimeMs();

        } else {

            this.currentAttemptedRequests =
                    0;

            this.currentSuccessfulRequests =
                    0;

            this.currentFailedRequests =
                    0;

            this.currentThroughputRequestsPerSecond =
                    0.0;

            this.currentAverageLatencyMs =
                    0.0;

            this.currentElapsedTimeMs =
                    0L;
        }

        this.errorMessage =
                errorMessage == null
                        ? ""
                        : errorMessage;
    }

    // ================================================================
    // GETTERS
    // ================================================================

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

    // ================================================================
    // LIVE METRIC GETTERS
    // ================================================================

    public int getCurrentAttemptedRequests() {
        return currentAttemptedRequests;
    }

    public int getCurrentSuccessfulRequests() {
        return currentSuccessfulRequests;
    }

    public int getCurrentFailedRequests() {
        return currentFailedRequests;
    }

    public double getCurrentThroughputRequestsPerSecond() {
        return currentThroughputRequestsPerSecond;
    }

    public double getCurrentAverageLatencyMs() {
        return currentAverageLatencyMs;
    }

    public long getCurrentElapsedTimeMs() {
        return currentElapsedTimeMs;
    }
}