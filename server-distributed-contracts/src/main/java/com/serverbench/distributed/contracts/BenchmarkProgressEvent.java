package com.serverbench.distributed.contracts;

import java.time.Instant;

public record BenchmarkProgressEvent(
        String eventId,
        String jobId,
        String experimentId,
        String runId,
        String agentId,
        int completedRequests,
        int attemptedRequests,
        double throughputRequestsPerSecond,
        double averageLatencyMs,
        Instant occurredAt
) {
}
