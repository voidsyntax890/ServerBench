package com.serverbench.backend.dto.response;

import java.util.List;

import com.serverbench.engine.benchmark.ServerArchitecture;

public class ArchitectureResponse {

    private final String architecture;
    private final String description;
    private final List<String> supportedSettings;

    public ArchitectureResponse(
            ServerArchitecture architecture
    ) {

        this.architecture =
                architecture.name();

        this.description =
                getDescription(architecture);

        this.supportedSettings =
                getSupportedSettings(architecture);
    }

    private String getDescription(
            ServerArchitecture architecture
    ) {

        return switch (architecture) {

            case SINGLE_THREADED ->
                    "Processes requests using a single execution path.";

            case MULTI_THREADED ->
                    "Uses a platform thread for each accepted request.";

            case THREAD_POOL ->
                    "Uses a bounded reusable pool of platform threads.";

            case VIRTUAL_THREAD ->
                    "Uses a virtual thread for each accepted request.";
        };
    }

    private List<String> getSupportedSettings(
            ServerArchitecture architecture
    ) {

        return switch (architecture) {

            case SINGLE_THREADED ->
                    List.of(
                            "concurrency",
                            "warmupDurationMs",
                            "requestTimeoutMs"
                    );

            case MULTI_THREADED ->
                    List.of(
                            "concurrency",
                            "warmupDurationMs",
                            "requestTimeoutMs"
                    );

            case THREAD_POOL ->
                    List.of(
                            "concurrency",
                            "threadPoolSize",
                            "warmupDurationMs",
                            "requestTimeoutMs"
                    );

            case VIRTUAL_THREAD ->
                    List.of(
                            "concurrency",
                            "warmupDurationMs",
                            "requestTimeoutMs"
                    );
        };
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSupportedSettings() {
        return supportedSettings;
    }
}