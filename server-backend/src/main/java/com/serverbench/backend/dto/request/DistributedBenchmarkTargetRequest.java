package com.serverbench.backend.dto.request;

import com.serverbench.engine.benchmark.ServerArchitecture;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DistributedBenchmarkTargetRequest {

    @NotNull(
            message = "Target architecture is required."
    )
    private ServerArchitecture architecture;

    @NotBlank(
            message = "Target host cannot be blank."
    )
    private String host;

    @NotNull(
            message = "Target port is required."
    )
    @Min(
            value = 1,
            message = "Target port must be between 1 and 65535."
    )
    @Max(
            value = 65535,
            message = "Target port must be between 1 and 65535."
    )
    private Integer port;

    public DistributedBenchmarkTargetRequest() {
    }

    public ServerArchitecture getArchitecture() {
        return architecture;
    }

    public void setArchitecture(
            ServerArchitecture architecture
    ) {
        this.architecture = architecture;
    }

    public String getHost() {
        return host;
    }

    public void setHost(
            String host
    ) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(
            Integer port
    ) {
        this.port = port;
    }
}