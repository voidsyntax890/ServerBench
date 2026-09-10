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
    // DISTRIBUTED EXECUTION METADATA
    // ================================================================

    /*
     * Kafka benchmark job ID.
     *
     * This is unique because the same Kafka result can be delivered
     * again after a retry/restart. The job ID gives us an idempotency
     * key so the backend does not create duplicate benchmark runs.
     */
    @Column(
            name = "distributed_job_id",
            unique = true,
            length = 100
    )
    private String distributedJobId;

    /*
     * Logical distributed run ID supplied by the controller.
     */
    @Column(
            name = "distributed_run_id",
            length = 100
    )
    private String distributedRunId;

    /*
     * Agent that actually executed this benchmark job.
     */
    @Column(
            name = "agent_id",
            length = 100
    )
    private String agentId;

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

    protected BenchmarkRunEntity() {
        /*
         * Required by JPA.
         */
    }

    /*
     * Existing constructor used by local execution.
     *
     * Keeping this constructor unchanged protects the current
     * local benchmark persistence path.
     */
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

    /*
     * Constructor used by distributed result aggregation.
     */
    public BenchmarkRunEntity(
            ExperimentEntity experiment,
            ServerArchitecture architecture,
            Integer repetitionNumber,
            Status status,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String distributedJobId,
            String distributedRunId,
            String agentId,
            String targetHost,
            Integer targetPort
    ) {

        this(
                experiment,
                architecture,
                repetitionNumber,
                status,
                errorMessage,
                startedAt,
                finishedAt
        );

        this.distributedJobId =
                distributedJobId;

        this.distributedRunId =
                distributedRunId;

        this.agentId =
                agentId;

        this.targetHost =
                targetHost;

        this.targetPort =
                targetPort;
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

    public String getDistributedJobId() {
        return distributedJobId;
    }

    public String getDistributedRunId() {
        return distributedRunId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public Integer getTargetPort() {
        return targetPort;
    }
}