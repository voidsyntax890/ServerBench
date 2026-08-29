package com.serverbench.backend.dto.response;

import com.serverbench.engine.benchmark.ExperimentRunResult;

import java.time.LocalDateTime;

public class ExperimentRunResponse {

    private final String architecture;
    private final int repetitionNumber;
    private final String status;
    private final String errorMessage;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final BenchmarkResultResponse benchmarkResult;

    public ExperimentRunResponse(
            ExperimentRunResult runResult
    ) {

        this.architecture =
                runResult
                        .getArchitecture()
                        .name();

        this.repetitionNumber =
                runResult.getRepetitionNumber();

        this.status =
                runResult
                        .getStatus()
                        .name();

        this.errorMessage =
                runResult.getErrorMessage();

        this.startedAt =
                runResult.getStartedAt();

        this.finishedAt =
                runResult.getFinishedAt();

        this.benchmarkResult =
                runResult.getBenchmarkResult() == null
                        ? null
                        : new BenchmarkResultResponse(
                                runResult
                                        .getBenchmarkResult()
                        );
    }

    public String getArchitecture() {
        return architecture;
    }

    public int getRepetitionNumber() {
        return repetitionNumber;
    }

    public String getStatus() {
        return status;
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

    public BenchmarkResultResponse getBenchmarkResult() {
        return benchmarkResult;
    }
}