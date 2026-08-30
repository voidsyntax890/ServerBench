package com.serverbench.backend.entity;

import com.serverbench.engine.benchmark.ServerArchitecture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "experiment_architectures",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_experiment_architecture",
                        columnNames = {
                                "experiment_id",
                                "architecture"
                        }
                )
        }
)
public class ExperimentArchitectureEntity {

    // ================================================================
    // PRIMARY KEY
    // ================================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // ================================================================
    // EXPERIMENT REFERENCE
    // ================================================================

    @Column(
            name = "experiment_id",
            nullable = false,
            length = 36
    )
    private String experimentId;

    // ================================================================
    // ARCHITECTURE
    // ================================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ServerArchitecture architecture;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================

    protected ExperimentArchitectureEntity() {
        /*
         * Required by JPA.
         */
    }

    public ExperimentArchitectureEntity(
            String experimentId,
            ServerArchitecture architecture
    ) {

        this.experimentId =
                experimentId;

        this.architecture =
                architecture;
    }

    // ================================================================
    // GETTERS
    // ================================================================

    public Long getId() {
        return id;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public ServerArchitecture getArchitecture() {
        return architecture;
    }
}