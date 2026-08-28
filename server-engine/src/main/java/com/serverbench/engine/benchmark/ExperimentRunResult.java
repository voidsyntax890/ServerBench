package com.serverbench.engine.benchmark;

import java.time.LocalDateTime;

public class ExperimentRunResult {

    public enum Status {
        COMPLETED,
        FAILED
    }

    private final ServerArchitecture architecture;
    private final int repetitionNumber;

    private final BenchmarkResult benchmarkResult;

    private final Status status;
    private final String errorMessage;

    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    public ExperimentRunResult(
            ServerArchitecture architecture,
            int repetitionNumber,
            BenchmarkResult benchmarkResult,
            Status status,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {

        if (architecture == null) {
            throw new IllegalArgumentException(
                    "Architecture cannot be null."
            );
        }

        if (repetitionNumber <= 0) {
            throw new IllegalArgumentException(
                    "Repetition number must be greater than 0."
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Run status cannot be null."
            );
        }

        if (startedAt == null) {
            throw new IllegalArgumentException(
                    "Start time cannot be null."
            );
        }

        if (finishedAt == null) {
            throw new IllegalArgumentException(
                    "Finish time cannot be null."
            );
        }

        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "Finish time cannot be before start time."
            );
        }

        if (status == Status.COMPLETED
                && benchmarkResult == null) {

            throw new IllegalArgumentException(
                    "Completed run must contain a benchmark result."
            );
        }

        this.architecture = architecture;
        this.repetitionNumber = repetitionNumber;
        this.benchmarkResult = benchmarkResult;
        this.status = status;
        this.errorMessage =
                errorMessage == null
                        ? ""
                        : errorMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public ServerArchitecture getArchitecture() {
        return architecture;
    }

    public int getRepetitionNumber() {
        return repetitionNumber;
    }

    public BenchmarkResult getBenchmarkResult() {
        return benchmarkResult;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return status == Status.COMPLETED;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }
}