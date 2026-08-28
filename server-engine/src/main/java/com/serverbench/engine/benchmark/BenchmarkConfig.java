package com.serverbench.engine.benchmark;

public class BenchmarkConfig {

    private final String host;
    private final int port;

    private final int totalRequests;
    private final int concurrency;

    private final long warmupDurationMs;
    private final long measurementDurationMs;
    private final int requestTimeoutMs;

    private final ExecutionMode executionMode;

    public BenchmarkConfig(
            String host,
            int port,
            int totalRequests,
            int concurrency,
            long warmupDurationMs,
            long measurementDurationMs,
            int requestTimeoutMs,
            ExecutionMode executionMode
    ) {
        this.host = host;
        this.port = port;
        this.totalRequests = totalRequests;
        this.concurrency = concurrency;
        this.warmupDurationMs = warmupDurationMs;
        this.measurementDurationMs = measurementDurationMs;
        this.requestTimeoutMs = requestTimeoutMs;
        this.executionMode = executionMode;

        validate();
    }

    private void validate() {

        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(
                    "Host cannot be empty."
            );
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535."
            );
        }

        if (concurrency <= 0) {
            throw new IllegalArgumentException(
                    "Concurrency must be greater than 0."
            );
        }

        if (warmupDurationMs < 0) {
            throw new IllegalArgumentException(
                    "Warm-up duration cannot be negative."
            );
        }

        if (requestTimeoutMs <= 0) {
            throw new IllegalArgumentException(
                    "Request timeout must be greater than 0."
            );
        }

        if (executionMode == null) {
            throw new IllegalArgumentException(
                    "Execution mode cannot be null."
            );
        }

        if (executionMode == ExecutionMode.REQUESTS
                && totalRequests <= 0) {

            throw new IllegalArgumentException(
                    "Total requests must be greater than 0 "
                            + "in REQUESTS mode."
            );
        }

        if (executionMode == ExecutionMode.DURATION
                && measurementDurationMs <= 0) {

            throw new IllegalArgumentException(
                    "Measurement duration must be greater than 0 "
                            + "in DURATION mode."
            );
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public long getWarmupDurationMs() {
        return warmupDurationMs;
    }

    public long getMeasurementDurationMs() {
        return measurementDurationMs;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }
}