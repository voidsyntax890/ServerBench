package com.serverbench.distributed.contracts;

import java.time.Instant;

public record BenchmarkResultEvent(
        String eventId,
        String jobId,
        String experimentId,
        String runId,
        String agentId,
        JobStatus status,
        String errorMessage,
        String architecture,
        int repetitionNumber,
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        long totalDurationMs,
        double throughputRequestsPerSecond,
        double averageLatencyMs,
        long minimumLatencyMs,
        long maximumLatencyMs,
        double p50LatencyMs,
        double p95LatencyMs,
        double p99LatencyMs,
        long connectTimeouts,
        long connectionRefused,
        long connectionResets,
        long readTimeouts,
        long noResponseFailures,
        long otherIoFailures,
        Instant startedAt,
        Instant finishedAt,
        Instant occurredAt
) {
}
