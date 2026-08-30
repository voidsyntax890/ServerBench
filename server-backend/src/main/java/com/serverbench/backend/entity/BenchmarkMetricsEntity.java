package com.serverbench.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "benchmark_metrics")
public class BenchmarkMetricsEntity {

    // ================================================================
    // PRIMARY KEY
    // ================================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // ================================================================
    // BENCHMARK RUN REFERENCE
    // ================================================================

    @OneToOne
    @JoinColumn(
            name = "run_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_benchmark_metrics_run"
            )
    )
    private BenchmarkRunEntity run;

    // ================================================================
    // REQUEST METRICS
    // ================================================================

    @Column(
            name = "total_requests",
            nullable = false
    )
    private Integer totalRequests;

    @Column(
            name = "successful_requests",
            nullable = false
    )
    private Integer successfulRequests;

    @Column(
            name = "failed_requests",
            nullable = false
    )
    private Integer failedRequests;

    // ================================================================
    // EXECUTION METRICS
    // ================================================================

    @Column(
            name = "total_duration_ms",
            nullable = false
    )
    private Long totalDurationMs;

    @Column(
            name = "throughput_requests_per_second",
            nullable = false
    )
    private Double throughputRequestsPerSecond;

    // ================================================================
    // LATENCY METRICS
    // ================================================================

    @Column(
            name = "average_latency_ms",
            nullable = false
    )
    private Double averageLatencyMs;

    @Column(
            name = "minimum_latency_ms",
            nullable = false
    )
    private Long minimumLatencyMs;

    @Column(
            name = "maximum_latency_ms",
            nullable = false
    )
    private Long maximumLatencyMs;

    @Column(
            name = "p50_latency_ms",
            nullable = false
    )
    private Double p50LatencyMs;

    @Column(
            name = "p95_latency_ms",
            nullable = false
    )
    private Double p95LatencyMs;

    @Column(
            name = "p99_latency_ms",
            nullable = false
    )
    private Double p99LatencyMs;

    // ================================================================
    // RELIABILITY METRICS
    // ================================================================

    @Column(
            name = "success_rate",
            nullable = false
    )
    private Double successRate;

    @Column(
            name = "error_rate",
            nullable = false
    )
    private Double errorRate;

    // ================================================================
    // FAILURE CATEGORIES
    // ================================================================

    @Column(
            name = "connect_timeouts",
            nullable = false
    )
    private Integer connectTimeouts;

    @Column(
            name = "connection_refused",
            nullable = false
    )
    private Integer connectionRefused;

    @Column(
            name = "connection_resets",
            nullable = false
    )
    private Integer connectionResets;

    @Column(
            name = "read_timeouts",
            nullable = false
    )
    private Integer readTimeouts;

    @Column(
            name = "no_response_failures",
            nullable = false
    )
    private Integer noResponseFailures;

    @Column(
            name = "other_io_failures",
            nullable = false
    )
    private Integer otherIoFailures;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================

    protected BenchmarkMetricsEntity() {
        /*
         * Required by JPA.
         */
    }

    public BenchmarkMetricsEntity(
            BenchmarkRunEntity run,
            Integer totalRequests,
            Integer successfulRequests,
            Integer failedRequests,
            Long totalDurationMs,
            Double throughputRequestsPerSecond,
            Double averageLatencyMs,
            Long minimumLatencyMs,
            Long maximumLatencyMs,
            Double p50LatencyMs,
            Double p95LatencyMs,
            Double p99LatencyMs,
            Double successRate,
            Double errorRate,
            Integer connectTimeouts,
            Integer connectionRefused,
            Integer connectionResets,
            Integer readTimeouts,
            Integer noResponseFailures,
            Integer otherIoFailures
    ) {

        this.run =
                run;

        this.totalRequests =
                totalRequests;

        this.successfulRequests =
                successfulRequests;

        this.failedRequests =
                failedRequests;

        this.totalDurationMs =
                totalDurationMs;

        this.throughputRequestsPerSecond =
                throughputRequestsPerSecond;

        this.averageLatencyMs =
                averageLatencyMs;

        this.minimumLatencyMs =
                minimumLatencyMs;

        this.maximumLatencyMs =
                maximumLatencyMs;

        this.p50LatencyMs =
                p50LatencyMs;

        this.p95LatencyMs =
                p95LatencyMs;

        this.p99LatencyMs =
                p99LatencyMs;

        this.successRate =
                successRate;

        this.errorRate =
                errorRate;

        this.connectTimeouts =
                connectTimeouts;

        this.connectionRefused =
                connectionRefused;

        this.connectionResets =
                connectionResets;

        this.readTimeouts =
                readTimeouts;

        this.noResponseFailures =
                noResponseFailures;

        this.otherIoFailures =
                otherIoFailures;
    }

    // ================================================================
    // GETTERS
    // ================================================================

    public Long getId() {
        return id;
    }

    public BenchmarkRunEntity getRun() {
        return run;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public Integer getSuccessfulRequests() {
        return successfulRequests;
    }

    public Integer getFailedRequests() {
        return failedRequests;
    }

    public Long getTotalDurationMs() {
        return totalDurationMs;
    }

    public Double getThroughputRequestsPerSecond() {
        return throughputRequestsPerSecond;
    }

    public Double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public Long getMinimumLatencyMs() {
        return minimumLatencyMs;
    }

    public Long getMaximumLatencyMs() {
        return maximumLatencyMs;
    }

    public Double getP50LatencyMs() {
        return p50LatencyMs;
    }

    public Double getP95LatencyMs() {
        return p95LatencyMs;
    }

    public Double getP99LatencyMs() {
        return p99LatencyMs;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public Integer getConnectTimeouts() {
        return connectTimeouts;
    }

    public Integer getConnectionRefused() {
        return connectionRefused;
    }

    public Integer getConnectionResets() {
        return connectionResets;
    }

    public Integer getReadTimeouts() {
        return readTimeouts;
    }

    public Integer getNoResponseFailures() {
        return noResponseFailures;
    }

    public Integer getOtherIoFailures() {
        return otherIoFailures;
    }
}