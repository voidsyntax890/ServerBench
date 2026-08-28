package com.serverbench.backend.dto.request;

import java.util.List;

import com.serverbench.backend.validation.ValidExecutionConfiguration;
import com.serverbench.engine.benchmark.ExecutionMode;
import com.serverbench.engine.benchmark.ServerArchitecture;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@ValidExecutionConfiguration
public class ExperimentRequest {

    @NotBlank(
            message = "Experiment name cannot be blank."
    )
    @Size(
            max = 100,
            message = "Experiment name cannot exceed 100 characters."
    )
    private String name;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters."
    )
    private String description;

    @NotBlank(
            message = "Host cannot be blank."
    )
    private String host;

    @NotNull(
            message = "Port is required."
    )
    @Min(
            value = 1,
            message = "Port must be between 1 and 65535."
    )
    @Max(
            value = 65535,
            message = "Port must be between 1 and 65535."
    )
    private Integer port;

    @NotNull(
            message = "Execution mode is required."
    )
    private ExecutionMode executionMode;

    /*
     * Required only when executionMode = REQUESTS.
     * Conditional validation is handled by
     * ValidExecutionConfiguration.
     */
    private Integer totalRequests;

    /*
     * Required only when executionMode = DURATION.
     * Conditional validation is handled by
     * ValidExecutionConfiguration.
     */
    private Long measurementDurationMs;

    @NotNull(
            message = "Concurrency is required."
    )
    @Positive(
            message = "Concurrency must be greater than 0."
    )
    private Integer concurrency;

    @NotNull(
            message = "Warm-up duration is required."
    )
    @Min(
            value = 0,
            message = "Warm-up duration cannot be negative."
    )
    private Long warmupDurationMs;

    @NotNull(
            message = "Request timeout is required."
    )
    @Positive(
            message = "Request timeout must be greater than 0."
    )
    private Integer requestTimeoutMs;

    @NotNull(
            message = "Repetitions is required."
    )
    @Positive(
            message = "Repetitions must be greater than 0."
    )
    private Integer repetitions;

    @NotEmpty(
            message = "At least one server architecture must be selected."
    )
    private List<ServerArchitecture> architectures;

    /*
     * Required only when THREAD_POOL is selected.
     * Conditional validation is handled by
     * ValidExecutionConfiguration.
     */
    private Integer threadPoolSize;

    public ExperimentRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(
            ExecutionMode executionMode
    ) {
        this.executionMode = executionMode;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(
            Integer totalRequests
    ) {
        this.totalRequests = totalRequests;
    }

    public Long getMeasurementDurationMs() {
        return measurementDurationMs;
    }

    public void setMeasurementDurationMs(
            Long measurementDurationMs
    ) {
        this.measurementDurationMs =
                measurementDurationMs;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    public Long getWarmupDurationMs() {
        return warmupDurationMs;
    }

    public void setWarmupDurationMs(
            Long warmupDurationMs
    ) {
        this.warmupDurationMs =
                warmupDurationMs;
    }

    public Integer getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(
            Integer requestTimeoutMs
    ) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(
            Integer repetitions
    ) {
        this.repetitions = repetitions;
    }

    public List<ServerArchitecture> getArchitectures() {
        return architectures;
    }

    public void setArchitectures(
            List<ServerArchitecture> architectures
    ) {
        this.architectures = architectures;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(
            Integer threadPoolSize
    ) {
        this.threadPoolSize = threadPoolSize;
    }
}