package com.serverbench.engine.benchmark;

public class BenchmarkMetricsSnapshot {

    private final String serverType;

    private final int attemptedRequests;
    private final int successfulRequests;
    private final int failedRequests;

    private final double throughputRequestsPerSecond;
    private final double averageLatencyMs;

    private final long elapsedTimeMs;

    public BenchmarkMetricsSnapshot(
            String serverType,
            int attemptedRequests,
            int successfulRequests,
            int failedRequests,
            double throughputRequestsPerSecond,
            double averageLatencyMs,
            long elapsedTimeMs
    ) {

        if (serverType == null || serverType.isBlank()) {
            throw new IllegalArgumentException(
                    "Server type cannot be null or blank."
            );
        }

        if (attemptedRequests < 0) {
            throw new IllegalArgumentException(
                    "Attempted requests cannot be negative."
            );
        }

        if (successfulRequests < 0) {
            throw new IllegalArgumentException(
                    "Successful requests cannot be negative."
            );
        }

        if (failedRequests < 0) {
            throw new IllegalArgumentException(
                    "Failed requests cannot be negative."
            );
        }

        if (throughputRequestsPerSecond < 0) {
            throw new IllegalArgumentException(
                    "Throughput cannot be negative."
            );
        }

        if (averageLatencyMs < 0) {
            throw new IllegalArgumentException(
                    "Average latency cannot be negative."
            );
        }

        if (elapsedTimeMs < 0) {
            throw new IllegalArgumentException(
                    "Elapsed time cannot be negative."
            );
        }

        this.serverType = serverType;
        this.attemptedRequests = attemptedRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.throughputRequestsPerSecond =
                throughputRequestsPerSecond;
        this.averageLatencyMs = averageLatencyMs;
        this.elapsedTimeMs = elapsedTimeMs;
    }

    public String getServerType() {
        return serverType;
    }

    public int getAttemptedRequests() {
        return attemptedRequests;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public double getThroughputRequestsPerSecond() {
        return throughputRequestsPerSecond;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public long getElapsedTimeMs() {
        return elapsedTimeMs;
    }
}