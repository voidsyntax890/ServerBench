package com.serverbench.backend.entity;

import java.time.LocalDateTime;

import com.serverbench.engine.benchmark.ExecutionMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiments")
public class ExperimentEntity {

    // ================================================================
    // PRIMARY KEY
    // ================================================================
    @Id
    @Column(
            nullable = false,
            updatable = false,
            length = 36
    )
    private String id;

    // ================================================================
    // EXPERIMENT INFORMATION
    // ================================================================
    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            length = 500
    )
    private String description;

    // ================================================================
    // BENCHMARK CONFIGURATION
    // ================================================================
    @Column(
            nullable = false,
            length = 255
    )
    private String host;

    @Column(
            nullable = false
    )
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "execution_mode",
            nullable = false,
            length = 20
    )
    private ExecutionMode executionMode;

    @Column(
            name = "total_requests"
    )
    private Integer totalRequests;

    @Column(
            name = "measurement_duration_ms"
    )
    private Long measurementDurationMs;

    @Column(
            nullable = false
    )
    private Integer concurrency;

    @Column(
            name = "warmup_duration_ms",
            nullable = false
    )
    private Long warmupDurationMs;

    @Column(
            name = "request_timeout_ms",
            nullable = false
    )
    private Integer requestTimeoutMs;

    @Column(
            nullable = false
    )
    private Integer repetitions;

    @Column(
            name = "thread_pool_size"
    )
    private Integer threadPoolSize;

    // ================================================================
    // EXPERIMENT STATUS
    // ================================================================
    @Column(
            nullable = false,
            length = 20
    )
    private String status;

    // ================================================================
    // ENVIRONMENT METADATA
    // ================================================================
    @Column(
            name = "operating_system",
            length = 255
    )
    private String operatingSystem;

    @Column(
            name = "java_version",
            length = 100
    )
    private String javaVersion;

    @Column(
            name = "java_runtime",
            length = 255
    )
    private String javaRuntime;

    @Column(
            length = 100
    )
    private String processor;

    @Column(
            name = "available_processors"
    )
    private Integer availableProcessors;

    @Column(
            name = "max_memory_mb"
    )
    private Long maxMemoryMb;

    // ================================================================
    // TIMESTAMP
    // ================================================================
    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================
    protected ExperimentEntity() {
        /*
         * Required by JPA.
         */
    }

    public ExperimentEntity(
            String id,
            String name,
            String description,
            String host,
            Integer port,
            ExecutionMode executionMode,
            Integer totalRequests,
            Long measurementDurationMs,
            Integer concurrency,
            Long warmupDurationMs,
            Integer requestTimeoutMs,
            Integer repetitions,
            Integer threadPoolSize,
            String status,
            String operatingSystem,
            String javaVersion,
            String javaRuntime,
            String processor,
            Integer availableProcessors,
            Long maxMemoryMb,
            LocalDateTime createdAt
    ) {

        this.id
                = id;

        this.name
                = name;

        this.description
                = description;

        this.host
                = host;

        this.port
                = port;

        this.executionMode
                = executionMode;

        this.totalRequests
                = totalRequests;

        this.measurementDurationMs
                = measurementDurationMs;

        this.concurrency
                = concurrency;

        this.warmupDurationMs
                = warmupDurationMs;

        this.requestTimeoutMs
                = requestTimeoutMs;

        this.repetitions
                = repetitions;

        this.threadPoolSize
                = threadPoolSize;

        this.status
                = status;

        this.operatingSystem
                = operatingSystem;

        this.javaVersion
                = javaVersion;

        this.javaRuntime
                = javaRuntime;

        this.processor
                = processor;

        this.availableProcessors
                = availableProcessors;

        this.maxMemoryMb
                = maxMemoryMb;

        this.createdAt
                = createdAt;
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

    public String getDescription() {
        return description;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
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

    public Integer getConcurrency() {
        return concurrency;
    }

    public Long getWarmupDurationMs() {
        return warmupDurationMs;
    }

    public Integer getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaRuntime() {
        return javaRuntime;
    }

    public String getProcessor() {
        return processor;
    }

    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    public Long getMaxMemoryMb() {
        return maxMemoryMb;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
