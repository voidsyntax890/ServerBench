package com.serverbench.engine.benchmark;

public class ExperimentProgress {

    private final String currentArchitecture;
    private final int currentRepetition;

    private final int completedRuns;
    private final int totalRuns;

    private final BenchmarkMetricsSnapshot metricsSnapshot;

    /*
     * Used for experiment-level progress updates.
     */
    public ExperimentProgress(
            String currentArchitecture,
            int currentRepetition,
            int completedRuns,
            int totalRuns
    ) {

        this(
                currentArchitecture,
                currentRepetition,
                completedRuns,
                totalRuns,
                null
        );
    }

    /*
     * Used for live benchmark metric updates.
     */
    public ExperimentProgress(
            String currentArchitecture,
            int currentRepetition,
            BenchmarkMetricsSnapshot metricsSnapshot
    ) {

        this(
                currentArchitecture,
                currentRepetition,
                0,
                0,
                metricsSnapshot
        );
    }

    private ExperimentProgress(
            String currentArchitecture,
            int currentRepetition,
            int completedRuns,
            int totalRuns,
            BenchmarkMetricsSnapshot metricsSnapshot
    ) {

        if (currentArchitecture == null
                || currentArchitecture.isBlank()) {

            throw new IllegalArgumentException(
                    "Current architecture cannot be null or blank."
            );
        }

        if (currentRepetition <= 0) {

            throw new IllegalArgumentException(
                    "Current repetition must be greater than zero."
            );
        }

        if (completedRuns < 0) {

            throw new IllegalArgumentException(
                    "Completed runs cannot be negative."
            );
        }

        if (totalRuns < 0) {

            throw new IllegalArgumentException(
                    "Total runs cannot be negative."
            );
        }

        if (totalRuns > 0
                && completedRuns > totalRuns) {

            throw new IllegalArgumentException(
                    "Completed runs cannot exceed total runs."
            );
        }

        this.currentArchitecture =
                currentArchitecture;

        this.currentRepetition =
                currentRepetition;

        this.completedRuns =
                completedRuns;

        this.totalRuns =
                totalRuns;

        this.metricsSnapshot =
                metricsSnapshot;
    }

    public String getCurrentArchitecture() {
        return currentArchitecture;
    }

    public int getCurrentRepetition() {
        return currentRepetition;
    }

    public int getCompletedRuns() {
        return completedRuns;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public double getProgressPercentage() {

        if (totalRuns <= 0) {
            return 0.0;
        }

        return ((double) completedRuns
                / totalRuns)
                * 100.0;
    }

    public BenchmarkMetricsSnapshot getMetricsSnapshot() {
        return metricsSnapshot;
    }

    public boolean hasMetricsSnapshot() {
        return metricsSnapshot != null;
    }
}