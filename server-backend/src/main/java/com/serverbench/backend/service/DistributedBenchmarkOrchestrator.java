package com.serverbench.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.serverbench.backend.dto.response.AgentRecordResponse;
import com.serverbench.backend.entity.ExperimentArchitectureEntity;
import com.serverbench.distributed.contracts.BenchmarkJob;
import com.serverbench.distributed.contracts.AgentStatus;
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

    /**
     * Creates and publishes the complete distributed experiment matrix:
     * every selected architecture multiplied by every configured repetition.
     */
    public List<BenchmarkJob> start(
            String experimentId
    ) {
        validateExperimentId(experimentId);

        Experiment experiment =
                experimentService.getExperiment(experimentId);

        if (experiment == null) {
            throw new IllegalArgumentException(
                    "Experiment not found: " + experimentId
            );
        }

        if (experimentService.getStatus(experimentId)
                != ExperimentService.ExperimentStatus.CREATED) {
            throw new IllegalStateException(
                    "Experiment must be in CREATED state before "
                            + "distributed execution can start."
            );
        }

        List<ExperimentArchitectureEntity> architectureTargets =
                experimentService.getDistributedArchitectureTargets(
                        experimentId
                );

        validateTargetCoverage(
                experiment,
                architectureTargets
        );

        int concurrency =
                experiment.getBenchmarkConfig().getConcurrency();

        for (ServerArchitecture architecture : experiment.getArchitectures()) {
            AgentRecordResponse agent =
                    findCompatibleAgent(
                            architecture,
                            concurrency
                    );

            if (agent == null) {
                throw new IllegalStateException(
                        "No healthy READY agent is available for architecture "
                                + architecture
                                + " and concurrency "
                                + concurrency
                );
            }
        }

        List<BenchmarkJob> jobs =
                createBenchmarkMatrix(
                        experiment,
                        architectureTargets
                );

        experimentService.markDistributedExperimentRunning(
                experimentId
        );

        int publishedJobs = 0;

        try {
            for (BenchmarkJob job : jobs) {
                distributedJobPublisher.publish(job);
                publishedJobs++;
            }

            return List.copyOf(jobs);

        } catch (RuntimeException exception) {

            if (publishedJobs == 0) {
                /*
                 * No job reached Kafka, so the experiment is still safe
                 * to retry from its original lifecycle state.
                 */
                experimentService.markDistributedExperimentCreated(
                        experimentId
                );
            } else {
                /*
                 * Some jobs are already in Kafka. Resetting to CREATED here
                 * would permit a second matrix to be launched on top of the
                 * first one. The experiment therefore remains RUNNING while
                 * already-published jobs finish or fail.
                 */
                experimentService.recordDistributedPublicationFailure(
                        experimentId,
                        "Distributed job publication failed after "
                                + publishedJobs
                                + " job(s) were published: "
                                + exception.getMessage()
                );
            }

            throw exception;
        }
    }

    private List<BenchmarkJob> createBenchmarkMatrix(
            Experiment experiment,
            List<ExperimentArchitectureEntity> architectureTargets
    ) {
        BenchmarkConfig config =
                experiment.getBenchmarkConfig();

        String executionMode =
                toDistributedExecutionMode(
                        config.getExecutionMode()
                );

        Integer totalRequests =
                config.getExecutionMode() == ExecutionMode.REQUESTS
                        ? config.getTotalRequests()
                        : null;

        Long measurementDurationMs =
                config.getExecutionMode() == ExecutionMode.DURATION
                        ? config.getMeasurementDurationMs()
                        : null;

        Integer threadPoolSize =
                resolveThreadPoolSize(experiment);

        List<BenchmarkJob> jobs = new ArrayList<>();

        for (ServerArchitecture architecture : experiment.getArchitectures()) {

            ExperimentArchitectureEntity target =
                    findTarget(
                            architectureTargets,
                            architecture
                    );

            for (int repetition = 1;
                    repetition <= experiment.getRepetitions();
                    repetition++) {

                jobs.add(
                        new BenchmarkJob(
                                UUID.randomUUID().toString(),
                                experiment.getId(),
                                UUID.randomUUID().toString(),
                                architecture.name(),
                                repetition,
                                target.getTargetHost(),
                                target.getTargetPort(),
                                executionMode,
                                totalRequests,
                                measurementDurationMs,
                                config.getConcurrency(),
                                config.getWarmupDurationMs(),
                                config.getRequestTimeoutMs(),
                                architecture == ServerArchitecture.THREAD_POOL
                                        ? threadPoolSize
                                        : null
                        )
                );
            }
        }

        return jobs;
    }

    private Integer resolveThreadPoolSize(
            Experiment experiment
    ) {
        if (!experiment.getArchitectures().contains(
                ServerArchitecture.THREAD_POOL
        )) {
            return null;
        }

        Integer threadPoolSize =
                experimentService.getThreadPoolSize(
                        experiment.getId()
                );

        if (threadPoolSize == null || threadPoolSize <= 0) {
            throw new IllegalStateException(
                    "Thread pool size must be greater than 0 when "
                            + "THREAD_POOL is selected."
            );
        }

        return threadPoolSize;
    }

    private void validateTargetCoverage(
            Experiment experiment,
            List<ExperimentArchitectureEntity> targets
    ) {
        if (targets == null || targets.isEmpty()) {
            throw new IllegalStateException(
                    "Distributed execution requires a target endpoint "
                            + "for every selected architecture."
            );
        }

        for (ServerArchitecture architecture : experiment.getArchitectures()) {
            ExperimentArchitectureEntity target =
                    findTarget(
                            targets,
                            architecture
                    );

            if (target.getTargetHost() == null
                    || target.getTargetHost().isBlank()
                    || target.getTargetPort() == null
                    || target.getTargetPort() < 1
                    || target.getTargetPort() > 65535) {
                throw new IllegalStateException(
                        "Distributed target is incomplete for architecture "
                                + architecture
                );
            }
        }
    }

    private ExperimentArchitectureEntity findTarget(
            List<ExperimentArchitectureEntity> targets,
            ServerArchitecture architecture
    ) {
        return targets.stream()
                .filter(target -> architecture == target.getArchitecture())
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No distributed target is configured for architecture "
                                        + architecture
                        )
                );
    }

    private AgentRecordResponse findCompatibleAgent(
            ServerArchitecture architecture,
            int concurrency
    ) {
        List<AgentRecordResponse> agents =
                agentRegistryService.getAgents();

        return agents.stream()
                .filter(AgentRecordResponse::healthy)
                .filter(agent -> agent.status() == AgentStatus.READY)
                .filter(agent -> agent.capability() != null)
                .filter(agent ->
                        agent.capability()
                                .supportedArchitectures()
                                .contains(architecture.name())
                )
                .filter(agent ->
                        concurrency <= agent.capability().maxConcurrency()
                )
                .findFirst()
                .orElse(null);
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

    private void validateExperimentId(
            String experimentId
    ) {
        if (experimentId == null || experimentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }
    }
}
