package com.serverbench.distributed.contracts;

public record BenchmarkJob(
        String jobId,
        String experimentId,
        String runId,
        String architecture,
        int repetitionNumber,
        String targetHost,
        int targetPort,
        String executionMode,
        Integer totalRequests,
        Long measurementDurationMs,
        int concurrency,
        long warmupDurationMs,
        int requestTimeoutMs,
        Integer threadPoolSize
) {
}
