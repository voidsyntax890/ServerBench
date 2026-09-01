package com.serverbench.engine.benchmark;

public class ExperimentProgress {

    private final ServerArchitecture currentArchitecture;
    private final int currentRepetition;

    private final int completedRuns;
    private final int totalRuns;

    public ExperimentProgress(
            ServerArchitecture currentArchitecture,
            int currentRepetition,
            int completedRuns,
            int totalRuns
    ) {

        if (currentArchitecture == null) {
            throw new IllegalArgumentException(
                    "Current architecture cannot be null."
            );
        }

        if (currentRepetition <= 0) {
            throw new IllegalArgumentException(
                    "Current repetition must be greater than 0."
            );
        }

        if (completedRuns < 0) {
            throw new IllegalArgumentException(
                    "Completed runs cannot be negative."
            );
        }

        if (totalRuns <= 0) {
            throw new IllegalArgumentException(
                    "Total runs must be greater than 0."
            );
        }

        if (completedRuns > totalRuns) {
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
    }

    public ServerArchitecture getCurrentArchitecture() {
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
        if (totalRuns == 0) {
            return 0.0;
        }

        return (
                (double) completedRuns /
                totalRuns
        ) * 100.0;
    }
}