package com.serverbench.backend.dto.response;

import java.time.Instant;

import com.serverbench.distributed.contracts.AgentCapability;
import com.serverbench.distributed.contracts.AgentStatus;

public record AgentRecordResponse(
        String agentId,
        String agentVersion,
        String hostName,
        AgentStatus status,
        AgentCapability capability,
        String activeJobId,
        Instant registeredAt,
        Instant lastHeartbeat,
        boolean healthy
) {
}
