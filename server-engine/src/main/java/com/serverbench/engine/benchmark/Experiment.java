package com.serverbench.engine.benchmark;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Experiment {

    private final String id;
    private final String name;
    private final String description;

    private final BenchmarkConfig benchmarkConfig;

    private final List<ServerArchitecture> architectures;

    private final int repetitions;

    private final EnvironmentMetadata environmentMetadata;

    private final LocalDateTime createdAt;

    private Experiment(
            String id,
            String name,
            String description,
            BenchmarkConfig benchmarkConfig,
            List<ServerArchitecture> architectures,
            int repetitions,
            EnvironmentMetadata environmentMetadata,
            LocalDateTime createdAt
    ) {

        validate(
                name,
                benchmarkConfig,
                architectures,
                repetitions
        );

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        if (environmentMetadata == null) {
            throw new IllegalArgumentException(
                    "Environment metadata cannot be null."
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Creation time cannot be null."
            );
        }

        this.id = id;
        this.name = name.trim();
        this.description =
                description == null
                        ? ""
                        : description.trim();
        this.benchmarkConfig =
                benchmarkConfig;
        this.architectures =
                new ArrayList<>(architectures);
        this.repetitions =
                repetitions;
        this.environmentMetadata =
                environmentMetadata;
        this.createdAt =
                createdAt;
    }

    public Experiment(
            String name,
            String description,
            BenchmarkConfig benchmarkConfig,
            List<ServerArchitecture> architectures,
            int repetitions
    ) {

        validate(
                name,
                benchmarkConfig,
                architectures,
                repetitions
        );

        this.id = UUID.randomUUID().toString();

        this.name = name.trim();

        this.description =
                description == null
                        ? ""
                        : description.trim();

        this.benchmarkConfig =
                benchmarkConfig;

        this.architectures =
                new ArrayList<>(architectures);

        this.repetitions =
                repetitions;

        this.environmentMetadata =
                EnvironmentMetadata.capture();

        this.createdAt =
                LocalDateTime.now();
    }

    private void validate(
            String name,
            BenchmarkConfig benchmarkConfig,
            List<ServerArchitecture> architectures,
            int repetitions
    ) {

        if (name == null || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment name cannot be empty."
            );
        }

        if (benchmarkConfig == null) {

            throw new IllegalArgumentException(
                    "Benchmark configuration cannot be null."
            );
        }

        if (architectures == null
                || architectures.isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one server architecture "
                            + "must be selected."
            );
        }

        if (architectures.contains(null)) {

            throw new IllegalArgumentException(
                    "Server architecture cannot be null."
            );
        }

        if (repetitions <= 0) {

            throw new IllegalArgumentException(
                    "Repetitions must be greater than 0."
            );
        }
    }

    public static Experiment restore(
            String id,
            String name,
            String description,
            BenchmarkConfig benchmarkConfig,
            List<ServerArchitecture> architectures,
            int repetitions,
            EnvironmentMetadata environmentMetadata,
            LocalDateTime createdAt
    ) {

        return new Experiment(
                id,
                name,
                description,
                benchmarkConfig,
                architectures,
                repetitions,
                environmentMetadata,
                createdAt
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BenchmarkConfig getBenchmarkConfig() {
        return benchmarkConfig;
    }

    public List<ServerArchitecture> getArchitectures() {

        return Collections.unmodifiableList(
                architectures
        );
    }

    public int getRepetitions() {
        return repetitions;
    }

    public EnvironmentMetadata getEnvironmentMetadata() {
        return environmentMetadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {

        return "Experiment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", benchmarkConfig=" + benchmarkConfig +
                ", architectures=" + architectures +
                ", repetitions=" + repetitions +
                ", environmentMetadata=" +
                environmentMetadata +
                ", createdAt=" + createdAt +
                '}';
    }
}