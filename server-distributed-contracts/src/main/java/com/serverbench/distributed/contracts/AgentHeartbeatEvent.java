package com.serverbench.distributed.contracts;

import java.time.Instant;

public record AgentHeartbeatEvent(
        String eventId,
        String agentId,
        AgentStatus status,
        String activeJobId,
        Instant occurredAt
) {
}
