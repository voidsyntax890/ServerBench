package com.serverbench.backend.dto.response;

import com.serverbench.engine.benchmark.BenchmarkResult;

public class BenchmarkResultResponse {

    private final int totalRequests;
    private final int successfulRequests;
    private final int failedRequests;

    private final long totalDurationMs;

    private final double averageLatencyMs;
    private final double throughputRequestsPerSecond;

    private final double successRate;
    private final double errorRate;

    private final long minimumLatencyMs;
    private final long maximumLatencyMs;

    private final double p50LatencyMs;
    private final double p95LatencyMs;
    private final double p99LatencyMs;

    private final int connectTimeouts;
    private final int connectionRefused;
    private final int connectionResets;
    private final int readTimeouts;
    private final int noResponseFailures;
    private final int otherIoFailures;

    public BenchmarkResultResponse(
            BenchmarkResult result
    ) {

        this.totalRequests =
                result.getTotalRequests();

        this.successfulRequests =
                result.getSuccessfulRequests();

        this.failedRequests =
                result.getFailedRequests();

        this.totalDurationMs =
                result.getTotalDurationMs();

        this.averageLatencyMs =
                result.getAverageLatencyMs();

        this.throughputRequestsPerSecond =
                result.getThroughputRequestsPerSecond();

        this.successRate =
                result.getSuccessRate();

        this.errorRate =
                result.getErrorRate();

        this.minimumLatencyMs =
                result.getMinimumLatencyMs();

        this.maximumLatencyMs =
                result.getMaximumLatencyMs();

        this.p50LatencyMs =
                result.getP50LatencyMs();

        this.p95LatencyMs =
                result.getP95LatencyMs();

        this.p99LatencyMs =
                result.getP99LatencyMs();

        this.connectTimeouts =
                result.getConnectTimeouts();

        this.connectionRefused =
                result.getConnectionRefused();

        this.connectionResets =
                result.getConnectionResets();

        this.readTimeouts =
                result.getReadTimeouts();

        this.noResponseFailures =
                result.getNoResponseFailures();

        this.otherIoFailures =
                result.getOtherIoFailures();
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public double getThroughputRequestsPerSecond() {
        return throughputRequestsPerSecond;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public long getMinimumLatencyMs() {
        return minimumLatencyMs;
    }

    public long getMaximumLatencyMs() {
        return maximumLatencyMs;
    }

    public double getP50LatencyMs() {
        return p50LatencyMs;
    }

    public double getP95LatencyMs() {
        return p95LatencyMs;
    }

    public double getP99LatencyMs() {
        return p99LatencyMs;
    }

    public int getConnectTimeouts() {
        return connectTimeouts;
    }

    public int getConnectionRefused() {
        return connectionRefused;
    }

    public int getConnectionResets() {
        return connectionResets;
    }

    public int getReadTimeouts() {
        return readTimeouts;
    }

    public int getNoResponseFailures() {
        return noResponseFailures;
    }

    public int getOtherIoFailures() {
        return otherIoFailures;
    }
}