package com.serverbench.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.serverbench.engine.benchmark.EnvironmentMetadata;
import com.serverbench.engine.benchmark.ExecutionMode;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ServerArchitecture;

public class ExperimentResponse {

    private final String id;
    private final String name;
    private final String description;

    private final String host;
    private final int port;

    private final ExecutionMode executionMode;

    private final Integer totalRequests;
    private final Long measurementDurationMs;

    private final int concurrency;
    private final long warmupDurationMs;
    private final int requestTimeoutMs;

    private final int repetitions;

    private final List<ServerArchitecture> architectures;

    private final Integer threadPoolSize;

    private final EnvironmentMetadata environmentMetadata;

    private final LocalDateTime createdAt;

    private final String status;

    public ExperimentResponse(
            Experiment experiment,
            Integer threadPoolSize,
            String status
    ) {

        this.id =
                experiment.getId();

        this.name =
                experiment.getName();

        this.description =
                experiment.getDescription();

        this.host =
                experiment
                        .getBenchmarkConfig()
                        .getHost();

        this.port =
                experiment
                        .getBenchmarkConfig()
                        .getPort();

        this.executionMode =
                experiment
                        .getBenchmarkConfig()
                        .getExecutionMode();

        this.totalRequests =
                experiment
                        .getBenchmarkConfig()
                        .getTotalRequests();

        this.measurementDurationMs =
                experiment
                        .getBenchmarkConfig()
                        .getMeasurementDurationMs();

        this.concurrency =
                experiment
                        .getBenchmarkConfig()
                        .getConcurrency();

        this.warmupDurationMs =
                experiment
                        .getBenchmarkConfig()
                        .getWarmupDurationMs();

        this.requestTimeoutMs =
                experiment
                        .getBenchmarkConfig()
                        .getRequestTimeoutMs();

        this.repetitions =
                experiment.getRepetitions();

        this.architectures =
                experiment.getArchitectures();

        this.threadPoolSize =
                threadPoolSize;

        this.environmentMetadata =
                experiment
                        .getEnvironmentMetadata();

        this.createdAt =
                experiment.getCreatedAt();

        this.status =
                status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public Long getMeasurementDurationMs() {
        return measurementDurationMs;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public long getWarmupDurationMs() {
        return warmupDurationMs;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public List<ServerArchitecture> getArchitectures() {
        return architectures;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public EnvironmentMetadata getEnvironmentMetadata() {
        return environmentMetadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
}