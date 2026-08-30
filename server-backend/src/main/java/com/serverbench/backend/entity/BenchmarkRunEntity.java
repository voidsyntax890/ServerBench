package com.serverbench.backend.entity;

import java.time.LocalDateTime;

import com.serverbench.engine.benchmark.ExperimentRunResult.Status;
import com.serverbench.engine.benchmark.ServerArchitecture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "benchmark_runs")
public class BenchmarkRunEntity {

    // ================================================================
    // PRIMARY KEY
    // ================================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    @Column(
            nullable = false,
            updatable = false,
            length = 36
    )
    private String id;

    // ================================================================
    // EXPERIMENT REFERENCE
    // ================================================================

    @ManyToOne
    @JoinColumn(
            name = "experiment_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_benchmark_run_experiment"
            )
    )
    private ExperimentEntity experiment;

    // ================================================================
    // RUN INFORMATION
    // ================================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ServerArchitecture architecture;

    @Column(
            name = "repetition_number",
            nullable = false
    )
    private Integer repetitionNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Status status;

    @Column(
            name = "error_message",
            length = 2000
    )
    private String errorMessage;

    @Column(
            name = "started_at",
            nullable = false
    )
    private LocalDateTime startedAt;

    @Column(
            name = "finished_at"
    )
    private LocalDateTime finishedAt;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================

    protected BenchmarkRunEntity() {
        /*
         * Required by JPA.
         */
    }

    public BenchmarkRunEntity(
            ExperimentEntity experiment,
            ServerArchitecture architecture,
            Integer repetitionNumber,
            Status status,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {

        this.experiment =
                experiment;

        this.architecture =
                architecture;

        this.repetitionNumber =
                repetitionNumber;

        this.status =
                status;

        this.errorMessage =
                errorMessage;

        this.startedAt =
                startedAt;

        this.finishedAt =
                finishedAt;
    }

    // ================================================================
    // GETTERS
    // ================================================================

    public String getId() {
        return id;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public ServerArchitecture getArchitecture() {
        return architecture;
    }

    public Integer getRepetitionNumber() {
        return repetitionNumber;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }
}