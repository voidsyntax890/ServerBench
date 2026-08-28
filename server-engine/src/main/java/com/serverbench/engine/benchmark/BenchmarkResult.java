package com.serverbench.engine.benchmark;

public class BenchmarkResult {

    private final String serverType;

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

    public BenchmarkResult(
            String serverType,
            int totalRequests,
            int successfulRequests,
            int failedRequests,
            long totalDurationMs,
            double averageLatencyMs,
            double throughputRequestsPerSecond,
            double successRate,
            double errorRate,
            long minimumLatencyMs,
            long maximumLatencyMs,
            double p50LatencyMs,
            double p95LatencyMs,
            double p99LatencyMs,
            int connectTimeouts,
            int connectionRefused,
            int connectionResets,
            int readTimeouts,
            int noResponseFailures,
            int otherIoFailures
    ) {
        this.serverType = serverType;
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.totalDurationMs = totalDurationMs;
        this.averageLatencyMs = averageLatencyMs;
        this.throughputRequestsPerSecond = throughputRequestsPerSecond;
        this.successRate = successRate;
        this.errorRate = errorRate;
        this.minimumLatencyMs = minimumLatencyMs;
        this.maximumLatencyMs = maximumLatencyMs;
        this.p50LatencyMs = p50LatencyMs;
        this.p95LatencyMs = p95LatencyMs;
        this.p99LatencyMs = p99LatencyMs;
        this.connectTimeouts = connectTimeouts;
        this.connectionRefused = connectionRefused;
        this.connectionResets = connectionResets;
        this.readTimeouts = readTimeouts;
        this.noResponseFailures = noResponseFailures;
        this.otherIoFailures = otherIoFailures;
    }

    public String getServerType() {
        return serverType;
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

    public void printResult() {

        System.out.println();
        System.out.println("========== SERVERBENCH RESULT ==========");

        System.out.println("Server Type: " + serverType);

        System.out.println("Total Requests: " + totalRequests);

        System.out.println(
                "Successful Requests: "
                        + successfulRequests
        );

        System.out.println(
                "Failed Requests: "
                        + failedRequests
        );

        System.out.printf(
                "Success Rate: %.2f%%%n",
                successRate
        );

        System.out.printf(
                "Error Rate: %.2f%%%n",
                errorRate
        );

        System.out.println(
                "Total Duration: "
                        + totalDurationMs
                        + " ms"
        );

        System.out.printf(
                "Throughput: %.2f requests/sec%n",
                throughputRequestsPerSecond
        );

        System.out.printf(
                "Average Latency: %.2f ms%n",
                averageLatencyMs
        );

        System.out.println(
                "Minimum Latency: "
                        + minimumLatencyMs
                        + " ms"
        );

        System.out.println(
                "Maximum Latency: "
                        + maximumLatencyMs
                        + " ms"
        );

        System.out.printf(
                "p50 Latency: %.2f ms%n",
                p50LatencyMs
        );

        System.out.printf(
                "p95 Latency: %.2f ms%n",
                p95LatencyMs
        );

        System.out.printf(
                "p99 Latency: %.2f ms%n",
                p99LatencyMs
        );

        if (failedRequests > 0) {
            System.out.println();
            System.out.println("Failure Breakdown:");
            System.out.println(
                    "Connect Timeouts: " + connectTimeouts
            );
            System.out.println(
                    "Connection Refused: " + connectionRefused
            );
            System.out.println(
                    "Connection Resets/Closed: " + connectionResets
            );
            System.out.println(
                    "Read Timeouts: " + readTimeouts
            );
            System.out.println(
                    "No Response: " + noResponseFailures
            );
            System.out.println(
                    "Other I/O Failures: " + otherIoFailures
            );
        }

        System.out.println(
                "========================================"
        );
    }
}