package com.serverbench.backend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverbench.backend.entity.ExperimentArchitectureEntity;
import com.serverbench.backend.entity.BenchmarkMetricsEntity;
import com.serverbench.backend.entity.BenchmarkRunEntity;
import com.serverbench.backend.entity.ExperimentEntity;
import com.serverbench.backend.repository.BenchmarkMetricsRepository;
import com.serverbench.backend.repository.BenchmarkRunRepository;
import com.serverbench.backend.repository.ExperimentArchitectureRepository;
import com.serverbench.backend.repository.ExperimentRepository;
import com.serverbench.distributed.contracts.BenchmarkResultEvent;
import com.serverbench.distributed.contracts.JobStatus;
import com.serverbench.engine.benchmark.ExperimentRunResult.Status;
import com.serverbench.engine.benchmark.ServerArchitecture;

@Service
public class DistributedBenchmarkResultService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    DistributedBenchmarkResultService.class
            );

    private final ExperimentRepository experimentRepository;

    private final ExperimentArchitectureRepository
            experimentArchitectureRepository;

    private final BenchmarkRunRepository
            benchmarkRunRepository;

    private final BenchmarkMetricsRepository
            benchmarkMetricsRepository;

    private final ExperimentService
            experimentService;

    public DistributedBenchmarkResultService(
            ExperimentRepository experimentRepository,
            ExperimentArchitectureRepository
                    experimentArchitectureRepository,
            BenchmarkRunRepository benchmarkRunRepository,
            BenchmarkMetricsRepository
                    benchmarkMetricsRepository,
            ExperimentService experimentService
    ) {

        this.experimentRepository =
                experimentRepository;

        this.experimentArchitectureRepository =
                experimentArchitectureRepository;

        this.benchmarkRunRepository =
                benchmarkRunRepository;

        this.benchmarkMetricsRepository =
                benchmarkMetricsRepository;

        this.experimentService =
                experimentService;
    }

    // ================================================================
    // ACCEPT DISTRIBUTED RESULT
    // ================================================================

    @Transactional
    public void acceptResult(
            BenchmarkResultEvent event
    ) {

        validateEvent(event);

        /*
         * Kafka can redeliver a message.
         *
         * The distributed job ID is our idempotency key.
         */
        if (benchmarkRunRepository
                .findByDistributedJobId(
                        event.jobId()
                )
                .isPresent()) {

            log.info(
                    "Ignoring duplicate distributed benchmark result: "
                            + "jobId={}",
                    event.jobId()
            );

            return;
        }

        ExperimentEntity experiment =
                experimentRepository
                        .findById(
                                event.experimentId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Experiment not found: "
                                                        + event
                                                        .experimentId()
                                        )
                        );

        ServerArchitecture architecture =
                parseArchitecture(
                        event.architecture()
                );

        validateDistributedTarget(
                event,
                experiment.getId(),
                architecture
        );

        Status runStatus =
                mapStatus(
                        event.status()
                );

        LocalDateTime startedAt =
                toLocalDateTime(
                        event.startedAt()
                );

        LocalDateTime finishedAt =
                toLocalDateTime(
                        event.finishedAt()
                );

        BenchmarkRunEntity runEntity =
                new BenchmarkRunEntity(
                        experiment,
                        architecture,
                        event.repetitionNumber(),
                        runStatus,
                        safeErrorMessage(
                                event.errorMessage()
                        ),
                        startedAt,
                        finishedAt,
                        event.jobId(),
                        event.runId(),
                        event.agentId(),
                        event.targetHost(),
                        event.targetPort()
                );

        BenchmarkRunEntity savedRun =
                benchmarkRunRepository.save(
                        runEntity
                );

        if (runStatus ==
                Status.COMPLETED) {

            BenchmarkMetricsEntity metricsEntity =
                    toMetricsEntity(
                            savedRun,
                            event
                    );

            benchmarkMetricsRepository.save(
                    metricsEntity
            );
        }

        log.info(
                "Persisted distributed benchmark result: "
                        + "jobId={}, experimentId={}, architecture={}, "
                        + "repetition={}, status={}, agentId={}",
                event.jobId(),
                event.experimentId(),
                event.architecture(),
                event.repetitionNumber(),
                event.status(),
                event.agentId()
        );

        updateExperimentCompletion(
                experiment
        );
    }

    // ================================================================
    // EXPERIMENT COMPLETION
    // ================================================================

    private void updateExperimentCompletion(
            ExperimentEntity experiment
    ) {

        List<BenchmarkRunEntity> runs =
                benchmarkRunRepository
                        .findByExperiment_Id(
                                experiment.getId()
                        );

        long distributedRunCount =
                runs.stream()
                        .filter(
                                run ->
                                        run.getDistributedJobId()
                                                != null
                        )
                        .count();

        int architectureCount =
                experimentArchitectureRepository
                        .findByExperimentId(
                                experiment.getId()
                        )
                        .size();

        int repetitions =
                experiment.getRepetitions();

        long expectedRuns =
                (long) architectureCount
                        * repetitions;

        if (expectedRuns <= 0) {
            return;
        }

        if (distributedRunCount < expectedRuns) {

            log.info(
                    "Distributed experiment still waiting for results: "
                            + "experimentId={}, received={}, expected={}",
                    experiment.getId(),
                    distributedRunCount,
                    expectedRuns
            );

            return;
        }

        /*
         * A distributed experiment is complete once every expected
         * architecture/repetition has produced a final result event.
         *
         * Individual runs may still be FAILED. This matches the
         * existing local experiment model where a failed run does not
         * prevent the experiment result from containing the other runs.
         */
        experiment.setStatus(
                ExperimentService.ExperimentStatus
                        .COMPLETED
                        .name()
        );

        experimentRepository.save(
                experiment
        );

        log.info(
                "Distributed experiment completed: "
                        + "experimentId={}, receivedRuns={}, expectedRuns={}",
                experiment.getId(),
                distributedRunCount,
                expectedRuns
        );

        /*
         * Refresh the in-memory ExperimentService state and notify
         * existing SSE subscribers.
         */
        experimentService
                .refreshDistributedExperimentState(
                        experiment.getId()
                );
    }

    // ================================================================
    // METRIC CONVERSION
    // ================================================================

    private BenchmarkMetricsEntity toMetricsEntity(
            BenchmarkRunEntity runEntity,
            BenchmarkResultEvent event
    ) {

        int totalRequests =
                toInt(
                        event.totalRequests(),
                        "totalRequests"
                );

        int successfulRequests =
                toInt(
                        event.successfulRequests(),
                        "successfulRequests"
                );

        int failedRequests =
                toInt(
                        event.failedRequests(),
                        "failedRequests"
                );

        int connectTimeouts =
                toInt(
                        event.connectTimeouts(),
                        "connectTimeouts"
                );

        int connectionRefused =
                toInt(
                        event.connectionRefused(),
                        "connectionRefused"
                );

        int connectionResets =
                toInt(
                        event.connectionResets(),
                        "connectionResets"
                );

        int readTimeouts =
                toInt(
                        event.readTimeouts(),
                        "readTimeouts"
                );

        int noResponseFailures =
                toInt(
                        event.noResponseFailures(),
                        "noResponseFailures"
                );

        int otherIoFailures =
                toInt(
                        event.otherIoFailures(),
                        "otherIoFailures"
                );

        validateRequestCounts(
                totalRequests,
                successfulRequests,
                failedRequests
        );

        double successRate =
                calculateRate(
                        successfulRequests,
                        totalRequests
                );

        double errorRate =
                calculateRate(
                        failedRequests,
                        totalRequests
                );

        return new BenchmarkMetricsEntity(
                runEntity,
                totalRequests,
                successfulRequests,
                failedRequests,
                event.totalDurationMs(),
                event.throughputRequestsPerSecond(),
                event.averageLatencyMs(),
                event.minimumLatencyMs(),
                event.maximumLatencyMs(),
                event.p50LatencyMs(),
                event.p95LatencyMs(),
                event.p99LatencyMs(),
                successRate,
                errorRate,
                connectTimeouts,
                connectionRefused,
                connectionResets,
                readTimeouts,
                noResponseFailures,
                otherIoFailures
        );
    }

    private void validateDistributedTarget(
            BenchmarkResultEvent event,
            String experimentId,
            ServerArchitecture architecture
    ) {
        if (event.targetHost() == null || event.targetHost().isBlank()) {
            throw new IllegalArgumentException(
                    "Distributed result target host cannot be empty."
            );
        }

        if (event.targetPort() < 1 || event.targetPort() > 65535) {
            throw new IllegalArgumentException(
                    "Distributed result target port must be between 1 and 65535."
            );
        }

        ExperimentArchitectureEntity target =
                experimentArchitectureRepository
                        .findByExperimentId(experimentId)
                        .stream()
                        .filter(entity -> architecture == entity.getArchitecture())
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No configured distributed target for architecture "
                                                + architecture
                                )
                        );

        if (!event.targetHost().equals(target.getTargetHost())
                || event.targetPort() != target.getTargetPort()) {
            throw new IllegalArgumentException(
                    "Distributed result target does not match the configured target for architecture "
                            + architecture
            );
        }
    }

    // ================================================================
    // VALIDATION
    // ================================================================

    private void validateEvent(
            BenchmarkResultEvent event
    ) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Benchmark result event cannot be null."
            );
        }

        requireText(
                event.jobId(),
                "Benchmark result job ID"
        );

        requireText(
                event.experimentId(),
                "Benchmark result experiment ID"
        );

        requireText(
                event.runId(),
                "Benchmark result run ID"
        );

        requireText(
                event.agentId(),
                "Benchmark result agent ID"
        );

        requireText(
                event.architecture(),
                "Benchmark result architecture"
        );

        if (event.repetitionNumber() <= 0) {
            throw new IllegalArgumentException(
                    "Benchmark result repetition number must be greater than 0."
            );
        }

        if (event.status() == null) {
            throw new IllegalArgumentException(
                    "Benchmark result status cannot be null."
            );
        }

        switch (event.status()) {

            case COMPLETED:
            case FAILED:
            case CANCELLED:
            case INCOMPLETE:
                break;

            case ASSIGNED:
            case RUNNING:
                throw new IllegalArgumentException(
                        "Non-final job status cannot be persisted "
                                + "as a benchmark result: "
                                + event.status()
                );

            default:
                throw new IllegalArgumentException(
                        "Unsupported benchmark result status: "
                                + event.status()
                );
        }

        if (event.startedAt() == null) {
            throw new IllegalArgumentException(
                    "Benchmark result startedAt cannot be null."
            );
        }

        if (event.finishedAt() == null) {
            throw new IllegalArgumentException(
                    "Benchmark result finishedAt cannot be null."
            );
        }

        if (event.finishedAt()
                .isBefore(event.startedAt())) {

            throw new IllegalArgumentException(
                    "Benchmark result finishedAt cannot be before startedAt."
            );
        }

        parseArchitecture(
                event.architecture()
        );

        if (event.status() ==
                JobStatus.COMPLETED) {

            validateCompletedMetrics(
                    event
            );
        }
    }

    private void validateCompletedMetrics(
            BenchmarkResultEvent event
    ) {

        if (event.totalRequests() < 0) {
            throw new IllegalArgumentException(
                    "Total requests cannot be negative."
            );
        }

        if (event.successfulRequests() < 0) {
            throw new IllegalArgumentException(
                    "Successful requests cannot be negative."
            );
        }

        if (event.failedRequests() < 0) {
            throw new IllegalArgumentException(
                    "Failed requests cannot be negative."
            );
        }

        if (event.successfulRequests()
                + event.failedRequests()
                != event.totalRequests()) {

            throw new IllegalArgumentException(
                    "Benchmark result request counts are inconsistent: "
                            + "successful + failed must equal total."
            );
        }

        requireNonNegative(
                event.totalDurationMs(),
                "Total duration"
        );

        requireFiniteNonNegative(
                event.throughputRequestsPerSecond(),
                "Throughput"
        );

        requireFiniteNonNegative(
                event.averageLatencyMs(),
                "Average latency"
        );

        requireNonNegative(
                event.minimumLatencyMs(),
                "Minimum latency"
        );

        requireNonNegative(
                event.maximumLatencyMs(),
                "Maximum latency"
        );

        requireFiniteNonNegative(
                event.p50LatencyMs(),
                "p50 latency"
        );

        requireFiniteNonNegative(
                event.p95LatencyMs(),
                "p95 latency"
        );

        requireFiniteNonNegative(
                event.p99LatencyMs(),
                "p99 latency"
        );

        requireNonNegative(
                event.connectTimeouts(),
                "Connect timeouts"
        );

        requireNonNegative(
                event.connectionRefused(),
                "Connection refused count"
        );

        requireNonNegative(
                event.connectionResets(),
                "Connection resets"
        );

        requireNonNegative(
                event.readTimeouts(),
                "Read timeouts"
        );

        requireNonNegative(
                event.noResponseFailures(),
                "No-response failures"
        );

        requireNonNegative(
                event.otherIoFailures(),
                "Other I/O failures"
        );
    }

    // ================================================================
    // STATUS / ARCHITECTURE MAPPING
    // ================================================================

    private Status mapStatus(
            JobStatus status
    ) {

        return switch (status) {

            case COMPLETED ->
                    Status.COMPLETED;

            case FAILED,
                 CANCELLED,
                 INCOMPLETE ->
                    Status.FAILED;

            case ASSIGNED,
                 RUNNING ->
                    throw new IllegalArgumentException(
                            "Non-final job status cannot be mapped "
                                    + "to a persisted final run."
                    );
        };
    }

    private ServerArchitecture parseArchitecture(
            String architecture
    ) {

        try {

            return ServerArchitecture.valueOf(
                    architecture.trim()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Unsupported benchmark architecture: "
                            + architecture,
                    exception
            );
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private LocalDateTime toLocalDateTime(
            Instant instant
    ) {

        return LocalDateTime.ofInstant(
                instant,
                ZoneId.systemDefault()
        );
    }

    private double calculateRate(
            int count,
            int total
    ) {

        if (total <= 0) {
            return 0.0;
        }

        return (count * 100.0)
                / total;
    }

    private int toInt(
            long value,
            String fieldName
    ) {

        if (value < 0) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be negative."
            );
        }

        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    fieldName
                            + " exceeds the database integer range."
            );
        }

        return (int) value;
    }

    private void validateRequestCounts(
            int totalRequests,
            int successfulRequests,
            int failedRequests
    ) {

        if (successfulRequests
                + failedRequests
                != totalRequests) {

            throw new IllegalArgumentException(
                    "Persisted request counts are inconsistent."
            );
        }
    }

    private void requireText(
            String value,
            String fieldName
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be empty."
            );
        }
    }

    private void requireNonNegative(
            long value,
            String fieldName
    ) {

        if (value < 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be negative."
            );
        }
    }

    private void requireFiniteNonNegative(
            double value,
            String fieldName
    ) {

        if (!Double.isFinite(value)
                || value < 0.0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be finite and non-negative."
            );
        }
    }

    private String safeErrorMessage(
            String errorMessage
    ) {

        if (errorMessage == null
                || errorMessage.isBlank()) {

            return "";
        }

        return errorMessage;
    }
}