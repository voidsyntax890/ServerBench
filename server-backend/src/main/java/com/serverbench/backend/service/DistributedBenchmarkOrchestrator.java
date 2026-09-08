package com.serverbench.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.serverbench.backend.dto.response.AgentRecordResponse;
import com.serverbench.distributed.contracts.AgentStatus;
import com.serverbench.distributed.contracts.BenchmarkJob;
import com.serverbench.engine.benchmark.BenchmarkConfig;
import com.serverbench.engine.benchmark.ExecutionMode;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ServerArchitecture;

@Service
public class DistributedBenchmarkOrchestrator {

    private final ExperimentService experimentService;
    private final AgentRegistryService agentRegistryService;
    private final DistributedJobPublisher distributedJobPublisher;

    public DistributedBenchmarkOrchestrator(
            ExperimentService experimentService,
            AgentRegistryService agentRegistryService,
            DistributedJobPublisher distributedJobPublisher
    ) {
        this.experimentService = experimentService;
        this.agentRegistryService = agentRegistryService;
        this.distributedJobPublisher = distributedJobPublisher;
    }

    public BenchmarkJob start(
            String experimentId,
            ServerArchitecture architecture
    ) {
        validateExperimentId(experimentId);

        if (architecture == null) {
            throw new IllegalArgumentException(
                    "Distributed benchmark architecture cannot be null."
            );
        }

        Experiment experiment =
                experimentService.getExperiment(experimentId);

        if (experiment == null) {
            throw new IllegalArgumentException(
                    "Experiment not found: " + experimentId
            );
        }

        validateExperimentArchitecture(
                experiment,
                architecture
        );

        if (experimentService.getStatus(experimentId)
                != ExperimentService.ExperimentStatus.CREATED) {

            throw new IllegalStateException(
                    "Experiment must be in CREATED state before "
                            + "distributed execution can start."
            );
        }

        AgentRecordResponse agent =
                findCompatibleAgent(
                        architecture,
                        experiment.getBenchmarkConfig()
                                .getConcurrency()
                );

        if (agent == null) {
            throw new IllegalStateException(
                    "No healthy READY agent is available for architecture "
                            + architecture
                            + " and concurrency "
                            + experiment.getBenchmarkConfig()
                                    .getConcurrency()
            );
        }

        String jobId =
                UUID.randomUUID().toString();

        String runId =
                UUID.randomUUID().toString();

        BenchmarkJob job =
                createBenchmarkJob(
                        experiment,
                        architecture,
                        jobId,
                        runId
                );

        experimentService.markDistributedExperimentRunning(
                experimentId
        );

        try {

            distributedJobPublisher.publish(job);

            return job;

        } catch (RuntimeException exception) {

            /*
             * Publishing failed after the experiment was marked RUNNING.
             * Restore the lifecycle state so the experiment can be retried.
             */
            experimentService.markDistributedExperimentCreated(
                    experimentId
            );

            throw exception;
        }
    }

    private AgentRecordResponse findCompatibleAgent(
            ServerArchitecture architecture,
            int concurrency
    ) {

        List<AgentRecordResponse> agents =
                agentRegistryService.getAgents();

        return agents.stream()
                .filter(AgentRecordResponse::healthy)
                .filter(agent ->
                        agent.status() == AgentStatus.READY
                )
                .filter(agent ->
                        agent.capability() != null
                )
                .filter(agent ->
                        agent.capability()
                                .supportedArchitectures()
                                .contains(
                                        architecture.name()
                                )
                )
                .filter(agent ->
                        concurrency
                                <= agent.capability()
                                        .maxConcurrency()
                )
                .findFirst()
                .orElse(null);
    }

    private BenchmarkJob createBenchmarkJob(
            Experiment experiment,
            ServerArchitecture architecture,
            String jobId,
            String runId
    ) {

        BenchmarkConfig config =
                experiment.getBenchmarkConfig();

        String executionMode =
                toDistributedExecutionMode(
                        config.getExecutionMode()
                );

        Integer totalRequests =
                config.getExecutionMode()
                        == ExecutionMode.REQUESTS
                        ? config.getTotalRequests()
                        : null;

        Long measurementDurationMs =
                config.getExecutionMode()
                        == ExecutionMode.DURATION
                        ? config.getMeasurementDurationMs()
                        : null;

        return new BenchmarkJob(
                jobId,
                experiment.getId(),
                runId,
                architecture.name(),
                1,
                config.getHost(),
                config.getPort(),
                executionMode,
                totalRequests,
                measurementDurationMs,
                config.getConcurrency(),
                config.getWarmupDurationMs(),
                config.getRequestTimeoutMs(),
                null
        );
    }

    private String toDistributedExecutionMode(
            ExecutionMode executionMode
    ) {
        if (executionMode == null) {
            throw new IllegalArgumentException(
                    "Experiment execution mode cannot be null."
            );
        }

        return switch (executionMode) {
            case REQUESTS -> "FIXED_REQUESTS";
            case DURATION -> "FIXED_DURATION";
        };
    }

    private void validateExperimentArchitecture(
            Experiment experiment,
            ServerArchitecture architecture
    ) {

        if (experiment.getArchitectures() == null
                || !experiment.getArchitectures()
                        .contains(architecture)) {

            throw new IllegalArgumentException(
                    "Architecture "
                            + architecture
                            + " is not selected for experiment "
                            + experiment.getId()
            );
        }
    }

    private void validateExperimentId(
            String experimentId
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }
    }
}