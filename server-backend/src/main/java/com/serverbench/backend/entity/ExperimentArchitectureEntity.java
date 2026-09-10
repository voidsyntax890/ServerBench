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
    // DISTRIBUTED BENCHMARK TARGET
    // ================================================================

    /*
     * These fields identify the independently provisioned target
     * server instance associated with this architecture for a
     * distributed experiment.
     *
     * They are intentionally stored at the architecture level because
     * different architectures may run on different target endpoints.
     *
     * They remain nullable so existing local experiments and existing
     * persisted architecture records remain compatible.
     */
    @Column(
            name = "target_host",
            length = 255
    )
    private String targetHost;

    @Column(
            name = "target_port"
    )
    private Integer targetPort;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================

    protected ExperimentArchitectureEntity() {
        /*
         * Required by JPA.
         */
    }

    /*
     * Existing constructor.
     *
     * Preserved so the existing experiment creation flow does not
     * break while distributed target configuration remains optional
     * at experiment creation time.
     */
    public ExperimentArchitectureEntity(
            String experimentId,
            ServerArchitecture architecture
    ) {

        this(
                experimentId,
                architecture,
                null,
                null
        );
    }

    /*
     * Distributed-target constructor.
     */
    public ExperimentArchitectureEntity(
            String experimentId,
            ServerArchitecture architecture,
            String targetHost,
            Integer targetPort
    ) {

        this.experimentId =
                experimentId;

        this.architecture =
                architecture;

        this.targetHost =
                targetHost;

        this.targetPort =
                targetPort;
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

    public String getTargetHost() {
        return targetHost;
    }

    public Integer getTargetPort() {
        return targetPort;
    }
}