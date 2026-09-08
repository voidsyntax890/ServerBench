package com.serverbench.distributed.contracts;

import java.time.Instant;

public record AgentRegistrationEvent(
        String eventId,
        String agentId,
        String agentVersion,
        String hostName,
        AgentStatus status,
        AgentCapability capability,
        Instant registeredAt
) {
}
