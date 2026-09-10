package com.serverbench.agent.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.serverbench.agent.AgentRegistrationService;
import com.serverbench.agent.kafka.BenchmarkProgressPublisher;
import com.serverbench.agent.kafka.BenchmarkResultPublisher;
import com.serverbench.distributed.contracts.BenchmarkJob;
import com.serverbench.distributed.contracts.BenchmarkProgressEvent;
import com.serverbench.distributed.contracts.BenchmarkResultEvent;
import com.serverbench.distributed.contracts.JobStatus;
import com.serverbench.engine.benchmark.BenchmarkConfig;
import com.serverbench.engine.benchmark.BenchmarkMetricsSnapshot;
import com.serverbench.engine.benchmark.BenchmarkResult;
import com.serverbench.engine.benchmark.BenchmarkRunner;
import com.serverbench.engine.benchmark.ExecutionMode;
import com.serverbench.engine.benchmark.ServerArchitecture;

@Service
public class DistributedBenchmarkExecutor {

    private static final Logger log =
            LoggerFactory.getLogger(
                    DistributedBenchmarkExecutor.class
            );

    private final BenchmarkProgressPublisher progressPublisher;
    private final BenchmarkResultPublisher resultPublisher;
    private final AgentRegistrationService agentRegistrationService;

    public DistributedBenchmarkExecutor(
            BenchmarkProgressPublisher progressPublisher,
            BenchmarkResultPublisher resultPublisher,
            AgentRegistrationService agentRegistrationService
    ) {
        this.progressPublisher =
                progressPublisher;

        this.resultPublisher =
                resultPublisher;

        this.agentRegistrationService =
                agentRegistrationService;
    }

    // ================================================================
    // EXECUTION
    // ================================================================

    public void execute(
            BenchmarkJob job
    ) {

        if (job == null) {

            log.error(
                    "Ignoring null benchmark job."
            );

            return;
        }

        Instant startedAt =
                Instant.now();

        try {

            validateJob(job);

            boolean acquired =
                    agentRegistrationService
                            .tryAcquireJob(
                                    job.jobId()
                            );

            if (!acquired) {

                log.warn(
                        "Agent could not acquire benchmark job: {}",
                        job.jobId()
                );

                publishFailure(
                        job,
                        startedAt,
                        Instant.now(),
                        "Agent is unavailable or already executing another job."
                );

                return;
            }

            try {

                log.info(
                        "Agent {} started benchmark job {}.",
                        agentRegistrationService.getAgentId(),
                        job.jobId()
                );

                publishInitialProgress(job);

                BenchmarkConfig benchmarkConfig =
                        createBenchmarkConfig(job);

                BenchmarkRunner benchmarkRunner =
                        new BenchmarkRunner(
                                benchmarkConfig,
                                job.architecture(),
                                snapshot ->
                                        publishProgress(
                                                job,
                                                snapshot
                                        )
                        );

                BenchmarkResult result =
                        benchmarkRunner.run();

                Instant finishedAt =
                        Instant.now();

                publishResult(
                        job,
                        result,
                        startedAt,
                        finishedAt
                );

                log.info(
                        "Agent {} completed benchmark job {}.",
                        agentRegistrationService.getAgentId(),
                        job.jobId()
                );

            } catch (Exception exception) {

                Instant finishedAt =
                        Instant.now();

                log.error(
                        "Benchmark job {} failed: {}",
                        job.jobId(),
                        exception.getMessage()
                );

                publishFailure(
                        job,
                        startedAt,
                        finishedAt,
                        buildErrorMessage(
                                exception
                        )
                );

            } finally {

                agentRegistrationService
                        .releaseJob(
                                job.jobId()
                        );
            }

        } catch (Exception exception) {

            Instant finishedAt =
                    Instant.now();

            log.error(
                    "Benchmark job {} was rejected before execution: {}",
                    job.jobId(),
                    exception.getMessage()
            );

            publishFailure(
                    job,
                    startedAt,
                    finishedAt,
                    buildErrorMessage(
                            exception
                    )
            );
        }
    }

    // ================================================================
    // BENCHMARK CONFIGURATION
    // ================================================================

    private BenchmarkConfig createBenchmarkConfig(
            BenchmarkJob job
    ) {

        ExecutionMode executionMode =
                switch (job.executionMode()) {

                    case "FIXED_REQUESTS" ->
                            ExecutionMode.REQUESTS;

                    case "FIXED_DURATION" ->
                            ExecutionMode.DURATION;

                    default ->
                            throw new IllegalArgumentException(
                                    "Unsupported execution mode: "
                                            + job.executionMode()
                            );
                };

        int totalRequests =
                job.totalRequests() == null
                        ? 0
                        : job.totalRequests();

        long measurementDurationMs =
                job.measurementDurationMs() == null
                        ? 0L
                        : job.measurementDurationMs();

        return new BenchmarkConfig(
                job.targetHost(),
                job.targetPort(),
                totalRequests,
                job.concurrency(),
                job.warmupDurationMs(),
                measurementDurationMs,
                job.requestTimeoutMs(),
                executionMode
        );
    }

    // ================================================================
    // INITIAL PROGRESS
    // ================================================================

    private void publishInitialProgress(
            BenchmarkJob job
    ) {

        BenchmarkProgressEvent event =
                new BenchmarkProgressEvent(
                        UUID.randomUUID().toString(),
                        job.jobId(),
                        job.experimentId(),
                        job.runId(),
                        agentRegistrationService
                                .getAgentId(),
                        0,
                        0,
                        0.0,
                        0.0,
                        Instant.now()
                );

        progressPublisher.publish(event);
    }

    // ================================================================
    // LIVE PROGRESS
    // ================================================================

    private void publishProgress(
            BenchmarkJob job,
            BenchmarkMetricsSnapshot snapshot
    ) {

        if (snapshot == null) {
            return;
        }

        BenchmarkProgressEvent event =
                new BenchmarkProgressEvent(
                        UUID.randomUUID().toString(),
                        job.jobId(),
                        job.experimentId(),
                        job.runId(),
                        agentRegistrationService
                                .getAgentId(),
                        snapshot.getSuccessfulRequests(),
                        snapshot.getAttemptedRequests(),
                        snapshot.getThroughputRequestsPerSecond(),
                        snapshot.getAverageLatencyMs(),
                        Instant.now()
                );

        progressPublisher.publish(event);
    }

    // ================================================================
    // FINAL RESULT
    // ================================================================

    private void publishResult(
            BenchmarkJob job,
            BenchmarkResult result,
            Instant startedAt,
            Instant finishedAt
    ) {

        if (result == null) {

            throw new IllegalStateException(
                    "Benchmark runner returned a null result."
            );
        }

        BenchmarkResultEvent event =
                new BenchmarkResultEvent(
                        UUID.randomUUID().toString(),
                        job.jobId(),
                        job.experimentId(),
                        job.runId(),
                        agentRegistrationService
                                .getAgentId(),
                        JobStatus.COMPLETED,
                        "",
                        result.getServerType(),
                        job.repetitionNumber(),
                        job.targetHost(),
                        job.targetPort(),
                        result.getTotalRequests(),
                        result.getSuccessfulRequests(),
                        result.getFailedRequests(),
                        result.getTotalDurationMs(),
                        result.getThroughputRequestsPerSecond(),
                        result.getAverageLatencyMs(),
                        result.getMinimumLatencyMs(),
                        result.getMaximumLatencyMs(),
                        result.getP50LatencyMs(),
                        result.getP95LatencyMs(),
                        result.getP99LatencyMs(),
                        result.getConnectTimeouts(),
                        result.getConnectionRefused(),
                        result.getConnectionResets(),
                        result.getReadTimeouts(),
                        result.getNoResponseFailures(),
                        result.getOtherIoFailures(),
                        startedAt,
                        finishedAt,
                        Instant.now()
                );

        resultPublisher.publish(event);
    }

    // ================================================================
    // FAILURE RESULT
    // ================================================================

    private void publishFailure(
            BenchmarkJob job,
            Instant startedAt,
            Instant finishedAt,
            String errorMessage
    ) {

        BenchmarkResultEvent event =
                new BenchmarkResultEvent(
                        UUID.randomUUID().toString(),
                        job.jobId(),
                        job.experimentId(),
                        job.runId(),
                        agentRegistrationService
                                .getAgentId(),
                        JobStatus.FAILED,
                        safeErrorMessage(
                                errorMessage
                        ),
                        safeArchitecture(
                                job.architecture()
                        ),
                        job.repetitionNumber(),
                        job.targetHost(),
                        job.targetPort(),
                        0,
                        0,
                        0,
                        0,
                        0.0,
                        0.0,
                        0,
                        0,
                        0.0,
                        0.0,
                        0.0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        startedAt,
                        finishedAt,
                        Instant.now()
                );

        try {

            resultPublisher.publish(event);

        } catch (RuntimeException exception) {

            log.error(
                    "Unable to publish failure result for job {}: {}",
                    job.jobId(),
                    exception.getMessage()
            );
        }
    }

    // ================================================================
    // VALIDATION
    // ================================================================

    private void validateJob(
            BenchmarkJob job
    ) {

        if (job.jobId() == null
                || job.jobId().isBlank()) {

            throw new IllegalArgumentException(
                    "Benchmark job ID cannot be empty."
            );
        }

        if (job.experimentId() == null
                || job.experimentId().isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        if (job.runId() == null
                || job.runId().isBlank()) {

            throw new IllegalArgumentException(
                    "Run ID cannot be empty."
            );
        }

        if (job.architecture() == null
                || job.architecture().isBlank()) {

            throw new IllegalArgumentException(
                    "Benchmark architecture cannot be empty."
            );
        }

        validateArchitecture(
                job.architecture()
        );

        if (job.targetHost() == null
                || job.targetHost().isBlank()) {

            throw new IllegalArgumentException(
                    "Benchmark target host cannot be empty."
            );
        }

        if (job.targetPort() < 1
                || job.targetPort() > 65535) {

            throw new IllegalArgumentException(
                    "Benchmark target port must be between 1 and 65535."
            );
        }

        if (job.executionMode() == null
                || job.executionMode().isBlank()) {

            throw new IllegalArgumentException(
                    "Benchmark execution mode cannot be empty."
            );
        }

        if (!"FIXED_REQUESTS".equals(
                job.executionMode()
        )
                && !"FIXED_DURATION".equals(
                job.executionMode()
        )) {

            throw new IllegalArgumentException(
                    "Unsupported benchmark execution mode: "
                            + job.executionMode()
            );
        }

        if (job.concurrency() <= 0) {

            throw new IllegalArgumentException(
                    "Benchmark concurrency must be greater than 0."
            );
        }

        if (job.concurrency()
                > agentRegistrationService
                        .getMaxConcurrency()) {

            throw new IllegalArgumentException(
                    "Requested concurrency "
                            + job.concurrency()
                            + " exceeds agent maximum concurrency "
                            + agentRegistrationService
                                    .getMaxConcurrency()
            );
        }

        if (job.warmupDurationMs() < 0) {

            throw new IllegalArgumentException(
                    "Warm-up duration cannot be negative."
            );
        }

        if (job.requestTimeoutMs() <= 0) {

            throw new IllegalArgumentException(
                    "Request timeout must be greater than 0."
            );
        }

        if ("FIXED_REQUESTS".equals(
                job.executionMode()
        )) {

            if (job.totalRequests() == null
                    || job.totalRequests() <= 0) {

                throw new IllegalArgumentException(
                        "Total requests must be greater than 0 "
                                + "for FIXED_REQUESTS."
                );
            }
        }

        if ("FIXED_DURATION".equals(
                job.executionMode()
        )) {

            if (job.measurementDurationMs() == null
                    || job.measurementDurationMs() <= 0) {

                throw new IllegalArgumentException(
                        "Measurement duration must be greater than 0 "
                                + "for FIXED_DURATION."
                );
            }
        }

        if ("THREAD_POOL".equals(
                job.architecture()
        )) {

            if (job.threadPoolSize() == null
                    || job.threadPoolSize() <= 0) {

                throw new IllegalArgumentException(
                        "Thread pool size must be greater than 0 "
                                + "for THREAD_POOL architecture."
                );
            }
        }

        if (job.repetitionNumber() <= 0) {

            throw new IllegalArgumentException(
                    "Repetition number must be greater than 0."
            );
        }
    }

    private void validateArchitecture(
            String architecture
    ) {

        try {

            ServerArchitecture.valueOf(
                    architecture
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Unsupported benchmark architecture: "
                            + architecture
            );
        }
    }

    // ================================================================
    // SAFE VALUES
    // ================================================================

    private String safeArchitecture(
            String architecture
    ) {

        return architecture == null
                || architecture.isBlank()
                ? "UNKNOWN"
                : architecture;
    }

    private String safeErrorMessage(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return "Unknown benchmark execution failure.";
        }

        return message;
    }

    private String buildErrorMessage(
            Exception exception
    ) {

        if (exception == null) {

            return "Unknown benchmark execution failure.";
        }

        String message =
                exception.getMessage();

        if (message == null
                || message.isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return exception
                .getClass()
                .getSimpleName()
                + ": "
                + message;
    }
}