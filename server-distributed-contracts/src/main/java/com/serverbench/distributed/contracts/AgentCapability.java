package com.serverbench.distributed.contracts;

import java.util.List;

public record AgentCapability(
        List<String> supportedArchitectures,
        int availableProcessors,
        long maxMemoryMb,
        int maxConcurrency
) {

    public AgentCapability {
        supportedArchitectures = supportedArchitectures == null
                ? List.of()
                : List.copyOf(supportedArchitectures);
    }
}
