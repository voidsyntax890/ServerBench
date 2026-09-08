package com.serverbench.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.serverbench.backend.dto.response.AgentRecordResponse;
import com.serverbench.distributed.contracts.AgentHeartbeatEvent;
import com.serverbench.distributed.contracts.AgentRegistrationEvent;
import com.serverbench.distributed.contracts.AgentStatus;

@Service
public class AgentRegistryService {

    private final Map<String, AgentRecord> agents =
            new ConcurrentHashMap<>();

    private final long heartbeatTimeoutMs;

    public AgentRegistryService(
            @Value("${serverbench.agents.heartbeat-timeout-ms:15000}")
            long heartbeatTimeoutMs
    ) {
        if (heartbeatTimeoutMs <= 0) {
            throw new IllegalArgumentException(
                    "Agent heartbeat timeout must be greater than 0."
            );
        }

        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }

    public AgentRecordResponse register(
            AgentRegistrationEvent event
    ) {
        validateRegistration(event);

        Instant now = Instant.now();

        AgentRecord record = new AgentRecord(
                event.agentId(),
                event.agentVersion(),
                event.hostName(),
                event.status() == AgentStatus.OFFLINE
                        ? AgentStatus.READY
                        : event.status(),
                event.capability(),
                null,
                event.registeredAt() == null
                        ? now
                        : event.registeredAt(),
                now
        );

        agents.put(event.agentId(), record);

        return toResponse(record, now);
    }

    public AgentRecordResponse heartbeat(
            AgentHeartbeatEvent event
    ) {
        if (event == null
                || isBlank(event.agentId())) {
            throw new IllegalArgumentException(
                    "Agent heartbeat must contain an agent ID."
            );
        }

        AgentRecord existing = agents.get(event.agentId());

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Unknown agent: " + event.agentId()
            );
        }

        Instant now = Instant.now();

        AgentStatus status = event.status() == null
                ? existing.status
                : event.status();

        AgentRecord updated = existing.withHeartbeat(
                status,
                event.activeJobId(),
                now
        );

        agents.put(event.agentId(), updated);

        return toResponse(updated, now);
    }

    public List<AgentRecordResponse> getAgents() {
        Instant now = Instant.now();

        return agents.values()
                .stream()
                .map(record -> toResponse(record, now))
                .sorted(
                        Comparator.comparing(
                                AgentRecordResponse::agentId
                        )
                )
                .toList();
    }

    public AgentRecordResponse getAgent(
            String agentId
    ) {
        if (isBlank(agentId)) {
            throw new IllegalArgumentException(
                    "Agent ID cannot be empty."
            );
        }

        AgentRecord record = agents.get(agentId);

        if (record == null) {
            throw new IllegalArgumentException(
                    "Agent not found: " + agentId
            );
        }

        return toResponse(
                record,
                Instant.now()
        );
    }

    private AgentRecordResponse toResponse(
            AgentRecord record,
            Instant now
    ) {
        boolean healthy =
                record.lastHeartbeat != null
                        && Duration.between(
                                record.lastHeartbeat,
                                now
                        ).toMillis() <= heartbeatTimeoutMs;

        AgentStatus effectiveStatus = healthy
                ? record.status
                : AgentStatus.OFFLINE;

        return new AgentRecordResponse(
                record.agentId,
                record.agentVersion,
                record.hostName,
                effectiveStatus,
                record.capability,
                record.activeJobId,
                record.registeredAt,
                record.lastHeartbeat,
                healthy
        );
    }

    private void validateRegistration(
            AgentRegistrationEvent event
    ) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Agent registration cannot be null."
            );
        }

        if (isBlank(event.agentId())) {
            throw new IllegalArgumentException(
                    "Agent registration must contain an agent ID."
            );
        }

        if (isBlank(event.agentVersion())) {
            throw new IllegalArgumentException(
                    "Agent registration must contain an agent version."
            );
        }

        if (event.capability() == null) {
            throw new IllegalArgumentException(
                    "Agent registration must contain capabilities."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record AgentRecord(
            String agentId,
            String agentVersion,
            String hostName,
            AgentStatus status,
            com.serverbench.distributed.contracts.AgentCapability capability,
            String activeJobId,
            Instant registeredAt,
            Instant lastHeartbeat
    ) {

        private AgentRecord withHeartbeat(
                AgentStatus status,
                String activeJobId,
                Instant lastHeartbeat
        ) {
            return new AgentRecord(
                    agentId,
                    agentVersion,
                    hostName,
                    status,
                    capability,
                    activeJobId,
                    registeredAt,
                    lastHeartbeat
            );
        }
    }
}
