package com.serverbench.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverbench.backend.dto.request.ExperimentRequest;
import com.serverbench.backend.entity.BenchmarkMetricsEntity;
import com.serverbench.backend.entity.BenchmarkRunEntity;
import com.serverbench.backend.entity.ExperimentArchitectureEntity;
import com.serverbench.backend.entity.ExperimentEntity;
import com.serverbench.backend.repository.BenchmarkMetricsRepository;
import com.serverbench.backend.repository.BenchmarkRunRepository;
import com.serverbench.backend.repository.ExperimentArchitectureRepository;
import com.serverbench.backend.repository.ExperimentRepository;
import com.serverbench.engine.benchmark.BenchmarkConfig;
import com.serverbench.engine.benchmark.BenchmarkResult;
import com.serverbench.engine.benchmark.ComparisonSummary;
import com.serverbench.engine.benchmark.EnvironmentMetadata;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ExperimentAnalyzer;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.ExperimentRunner;
import com.serverbench.engine.benchmark.ServerArchitecture;
import com.serverbench.engine.benchmark.ServerFactory;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

@Service
public class ExperimentService {

    private static final int DEFAULT_THREAD_POOL_SIZE = 1;

    private final ExperimentRepository experimentRepository;
    private final ExperimentArchitectureRepository
            experimentArchitectureRepository;
    private final BenchmarkRunRepository benchmarkRunRepository;
    private final BenchmarkMetricsRepository
            benchmarkMetricsRepository;

    /*
     * Runtime-only state.
     *
     * PostgreSQL is now the source for persisted experiments,
     * runs and metrics.
     */
    private final Map<String, ExperimentRecord> experiments
            = new ConcurrentHashMap<>();

    public ExperimentService(
            ExperimentRepository experimentRepository,
            ExperimentArchitectureRepository
                    experimentArchitectureRepository,
            BenchmarkRunRepository benchmarkRunRepository,
            BenchmarkMetricsRepository
                    benchmarkMetricsRepository
    ) {

        this.experimentRepository =
                experimentRepository;

        this.experimentArchitectureRepository =
                experimentArchitectureRepository;

        this.benchmarkRunRepository =
                benchmarkRunRepository;

        this.benchmarkMetricsRepository =
                benchmarkMetricsRepository;
    }

    // ================================================================
    // CREATE EXPERIMENT
    // ================================================================

    @Transactional
    public Experiment createExperiment(
            ExperimentRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Experiment request cannot be null."
            );
        }

        BenchmarkConfig benchmarkConfig =
                new BenchmarkConfig(
                        request.getHost(),
                        request.getPort(),
                        request.getTotalRequests() == null
                                ? 0
                                : request.getTotalRequests(),
                        request.getConcurrency(),
                        request.getWarmupDurationMs(),
                        request.getMeasurementDurationMs() == null
                                ? 0
                                : request.getMeasurementDurationMs(),
                        request.getRequestTimeoutMs(),
                        request.getExecutionMode()
                );

        Experiment experiment =
                new Experiment(
                        request.getName(),
                        request.getDescription(),
                        benchmarkConfig,
                        request.getArchitectures(),
                        request.getRepetitions()
                );

        ExperimentEntity experimentEntity =
                toExperimentEntity(
                        experiment,
                        request
                );

        experimentRepository.save(
                experimentEntity
        );

        persistArchitectures(
                experiment.getId(),
                experiment.getArchitectures()
        );

        ExperimentRecord record =
                new ExperimentRecord(
                        experiment,
                        request,
                        experimentEntity
                );

        experiments.put(
                experiment.getId(),
                record
        );

        return experiment;
    }

    // ================================================================
    // START EXPERIMENT
    // ================================================================

    public void startExperiment(
            String experimentId
    ) {

        ExperimentRecord record =
                getOrRestoreRuntimeRecord(
                        experimentId
                );

        synchronized (record) {

            if (record.status
                    == ExperimentStatus.RUNNING) {

                throw new IllegalStateException(
                        "Experiment is already running."
                );
            }

            if (record.status
                    == ExperimentStatus.COMPLETED) {

                throw new IllegalStateException(
                        "Experiment has already completed."
                );
            }

            if (record.status
                    == ExperimentStatus.CANCELLED) {

                throw new IllegalStateException(
                        "Experiment has been cancelled."
                );
            }

            if (record.status
                    == ExperimentStatus.FAILED) {

                throw new IllegalStateException(
                        "Experiment has already failed."
                );
            }

            record.status =
                    ExperimentStatus.RUNNING;

            record.experimentEntity.setStatus(
                    ExperimentStatus.RUNNING.name()
            );

            experimentRepository.save(
                    record.experimentEntity
            );

            record.executionFuture =
                    CompletableFuture.runAsync(
                            () -> executeExperiment(
                                    record
                            )
                    );
        }
    }

    // ================================================================
    // GET EXPERIMENT
    // ================================================================

    public Experiment getExperiment(
            String experimentId
    ) {

        ExperimentRecord runtimeRecord =
                experiments.get(
                        experimentId
                );

        if (runtimeRecord != null) {

            return runtimeRecord.experiment;
        }

        ExperimentEntity entity =
                getExperimentEntity(
                        experimentId
                );

        return restoreExperiment(
                entity
        );
    }

    // ================================================================
    // GET STATUS
    // ================================================================

    public ExperimentStatus getStatus(
            String experimentId
    ) {

        ExperimentRecord runtimeRecord =
                experiments.get(
                        experimentId
                );

        if (runtimeRecord != null) {

            return runtimeRecord.status;
        }

        ExperimentEntity entity =
                getExperimentEntity(
                        experimentId
                );

        return ExperimentStatus.valueOf(
                entity.getStatus()
        );
    }

    // ================================================================
    // GET RESULT
    // ================================================================

    public ExperimentResult getResult(
            String experimentId
    ) {

        ExperimentRecord runtimeRecord =
                experiments.get(
                        experimentId
                );

        if (runtimeRecord != null
                && runtimeRecord.result != null) {

            return runtimeRecord.result;
        }

        ExperimentEntity entity =
                getExperimentEntity(
                        experimentId
                );

        if (!ExperimentStatus.COMPLETED.name()
                .equals(entity.getStatus())) {

            return null;
        }

        return restoreExperimentResult(
                entity
        );
    }

    // ================================================================
    // GET ERROR
    // ================================================================

    public String getErrorMessage(
            String experimentId
    ) {

        ExperimentRecord runtimeRecord =
                experiments.get(
                        experimentId
                );

        if (runtimeRecord != null) {

            return runtimeRecord.errorMessage;
        }

        ExperimentEntity entity =
                getExperimentEntity(
                        experimentId
                );

        return entity.getStatus().equals(
                ExperimentStatus.FAILED.name()
        )
                ? "Persisted experiment failed."
                : "";
    }

    // ================================================================
    // GET COMPARISON
    // ================================================================

    public ComparisonSummary getComparisonSummary(
            String experimentId
    ) {

        ExperimentResult result =
                getResult(
                        experimentId
                );

        if (result == null) {

            throw new IllegalStateException(
                    "Experiment results are not available yet."
            );
        }

        ExperimentAnalyzer analyzer =
                new ExperimentAnalyzer();

        return analyzer.analyze(
                result
        );
    }

    // ================================================================
    // GET EXPERIMENT HISTORY
    // ================================================================

    public List<Experiment> getAllExperiments() {

        List<ExperimentEntity> entities =
                experimentRepository.findAll();

        entities.sort(
                Comparator.comparing(
                        ExperimentEntity::getCreatedAt
                ).reversed()
        );

        List<Experiment> experiments =
                new ArrayList<>();

        for (ExperimentEntity entity :
                entities) {

            experiments.add(
                    restoreExperiment(
                            entity
                    )
            );
        }

        return List.copyOf(
                experiments
        );
    }

    // ================================================================
    // GET THREAD POOL SIZE
    // ================================================================

    public Integer getThreadPoolSize(
            String experimentId
    ) {

        ExperimentRecord runtimeRecord =
                experiments.get(
                        experimentId
                );

        if (runtimeRecord != null) {

            return runtimeRecord.request
                    .getThreadPoolSize();
        }

        ExperimentEntity entity =
                getExperimentEntity(
                        experimentId
                );

        return entity.getThreadPoolSize();
    }

    // ================================================================
    // GET AVAILABLE ARCHITECTURES
    // ================================================================

    public List<ServerArchitecture>
    getAvailableArchitectures() {

        return List.of(
                ServerArchitecture.SINGLE_THREADED,
                ServerArchitecture.MULTI_THREADED,
                ServerArchitecture.THREAD_POOL,
                ServerArchitecture.VIRTUAL_THREAD
        );
    }

    // ================================================================
    // EXECUTION
    // ================================================================

    private void executeExperiment(
            ExperimentRecord record
    ) {

        try {

            Experiment experiment =
                    record.experiment;

            ExperimentRequest request =
                    record.request;

            int threadPoolSize =
                    determineThreadPoolSize(
                            request
                    );

            ServerConfig serverConfig =
                    new ServerConfig(
                            experiment
                                    .getBenchmarkConfig()
                                    .getPort(),
                            threadPoolSize,
                            false
                    );

            Map<
                    ServerArchitecture,
                    Supplier<ServerEngine>
                    > serverFactories =
                    ServerFactory.createFactories(
                            serverConfig
                    );

            ExperimentRunner runner =
                    new ExperimentRunner(
                            experiment,
                            serverFactories
                    );

            ExperimentResult result =
                    runner.run();

            persistBenchmarkResults(
                    record.experimentEntity,
                    result
            );

            synchronized (record) {

                record.result =
                        result;

                record.status =
                        ExperimentStatus.COMPLETED;

                record.experimentEntity.setStatus(
                        ExperimentStatus.COMPLETED.name()
                );

                experimentRepository.save(
                        record.experimentEntity
                );
            }

        } catch (Exception exception) {

            synchronized (record) {

                record.errorMessage =
                        buildErrorMessage(
                                exception
                        );

                record.status =
                        ExperimentStatus.FAILED;

                record.experimentEntity.setStatus(
                        ExperimentStatus.FAILED.name()
                );

                experimentRepository.save(
                        record.experimentEntity
                );
            }
        }
    }

    // ================================================================
    // PERSIST RUNS + METRICS
    // ================================================================

    private void persistBenchmarkResults(
            ExperimentEntity experimentEntity,
            ExperimentResult experimentResult
    ) {

        for (ExperimentRunResult runResult :
                experimentResult.getRunResults()) {

            BenchmarkRunEntity runEntity =
                    new BenchmarkRunEntity(
                            experimentEntity,
                            runResult.getArchitecture(),
                            runResult.getRepetitionNumber(),
                            runResult.getStatus(),
                            runResult.getErrorMessage(),
                            runResult.getStartedAt(),
                            runResult.getFinishedAt()
                    );

            BenchmarkRunEntity savedRun =
                    benchmarkRunRepository.save(
                            runEntity
                    );

            BenchmarkResult benchmarkResult =
                    runResult.getBenchmarkResult();

            if (benchmarkResult != null) {

                BenchmarkMetricsEntity metricsEntity =
                        toBenchmarkMetricsEntity(
                                savedRun,
                                benchmarkResult
                        );

                benchmarkMetricsRepository.save(
                        metricsEntity
                );
            }
        }
    }

    private BenchmarkMetricsEntity
    toBenchmarkMetricsEntity(
            BenchmarkRunEntity runEntity,
            BenchmarkResult result
    ) {

        return new BenchmarkMetricsEntity(
                runEntity,
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
                result.getSuccessRate(),
                result.getErrorRate(),
                result.getConnectTimeouts(),
                result.getConnectionRefused(),
                result.getConnectionResets(),
                result.getReadTimeouts(),
                result.getNoResponseFailures(),
                result.getOtherIoFailures()
        );
    }

    // ================================================================
    // RESTORE EXPERIMENT
    // ================================================================

    private Experiment restoreExperiment(
            ExperimentEntity entity
    ) {

        List<
                ExperimentArchitectureEntity
                > architectureEntities =
                experimentArchitectureRepository
                        .findByExperimentId(
                                entity.getId()
                        );

        List<ServerArchitecture> architectures =
                architectureEntities
                        .stream()
                        .map(
                                ExperimentArchitectureEntity
                                        ::getArchitecture
                        )
                        .toList();

        BenchmarkConfig benchmarkConfig =
                new BenchmarkConfig(
                        entity.getHost(),
                        entity.getPort(),
                        entity.getTotalRequests() == null
                                ? 0
                                : entity.getTotalRequests(),
                        entity.getConcurrency(),
                        entity.getWarmupDurationMs(),
                        entity.getMeasurementDurationMs() == null
                                ? 0
                                : entity.getMeasurementDurationMs(),
                        entity.getRequestTimeoutMs(),
                        entity.getExecutionMode()
                );

        EnvironmentMetadata environmentMetadata =
                new EnvironmentMetadata(
                        entity.getOperatingSystem(),
                        entity.getJavaVersion(),
                        entity.getJavaRuntime(),
                        entity.getProcessor(),
                        entity.getAvailableProcessors(),
                        entity.getMaxMemoryMb()
                );

        return Experiment.restore(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                benchmarkConfig,
                architectures,
                entity.getRepetitions(),
                environmentMetadata,
                entity.getCreatedAt()
        );
    }

    // ================================================================
    // RESTORE RESULT
    // ================================================================

    private ExperimentResult restoreExperimentResult(
            ExperimentEntity experimentEntity
    ) {

        List<BenchmarkRunEntity> runEntities =
                benchmarkRunRepository
                        .findByExperiment_Id(
                                experimentEntity.getId()
                        );

        runEntities.sort(
                Comparator.comparing(
                        BenchmarkRunEntity::getStartedAt
                )
        );

        ExperimentResult result =
                new ExperimentResult(
                        experimentEntity.getId(),
                        experimentEntity.getName()
                );

        for (BenchmarkRunEntity runEntity :
                runEntities) {

            BenchmarkMetricsEntity metrics =
                    benchmarkMetricsRepository
                            .findByRun_Id(
                                    runEntity.getId()
                            )
                            .orElse(null);

            BenchmarkResult benchmarkResult =
                    metrics == null
                            ? null
                            : restoreBenchmarkResult(
                                    runEntity,
                                    metrics
                            );

            ExperimentRunResult runResult =
                    new ExperimentRunResult(
                            runEntity.getArchitecture(),
                            runEntity.getRepetitionNumber(),
                            benchmarkResult,
                            runEntity.getStatus(),
                            runEntity.getErrorMessage(),
                            runEntity.getStartedAt(),
                            runEntity.getFinishedAt()
                    );

            result.addRunResult(
                    runResult
            );
        }

        return result;
    }

    // ================================================================
    // RESTORE BENCHMARK RESULT
    // ================================================================

    private BenchmarkResult restoreBenchmarkResult(
            BenchmarkRunEntity runEntity,
            BenchmarkMetricsEntity metrics
    ) {

        return new BenchmarkResult(
                runEntity
                        .getArchitecture()
                        .name(),

                metrics.getTotalRequests(),
                metrics.getSuccessfulRequests(),
                metrics.getFailedRequests(),
                metrics.getTotalDurationMs(),
                metrics.getAverageLatencyMs(),
                metrics.getThroughputRequestsPerSecond(),
                metrics.getSuccessRate(),
                metrics.getErrorRate(),
                metrics.getMinimumLatencyMs(),
                metrics.getMaximumLatencyMs(),
                metrics.getP50LatencyMs(),
                metrics.getP95LatencyMs(),
                metrics.getP99LatencyMs(),
                metrics.getConnectTimeouts(),
                metrics.getConnectionRefused(),
                metrics.getConnectionResets(),
                metrics.getReadTimeouts(),
                metrics.getNoResponseFailures(),
                metrics.getOtherIoFailures()
        );
    }

    // ================================================================
    // THREAD POOL CONFIGURATION
    // ================================================================

    private int determineThreadPoolSize(
            ExperimentRequest request
    ) {

        if (request.getArchitectures().contains(
                ServerArchitecture.THREAD_POOL
        )) {

            Integer size =
                    request.getThreadPoolSize();

            if (size == null || size <= 0) {

                throw new IllegalArgumentException(
                        "Thread pool size must be greater than 0 "
                                + "when THREAD_POOL is selected."
                );
            }

            return size;
        }

        return DEFAULT_THREAD_POOL_SIZE;
    }

    // ================================================================
    // ENTITY CONVERSION
    // ================================================================

    private ExperimentEntity toExperimentEntity(
            Experiment experiment,
            ExperimentRequest request
    ) {

        BenchmarkConfig config =
                experiment.getBenchmarkConfig();

        EnvironmentMetadata metadata =
                experiment.getEnvironmentMetadata();

        return new ExperimentEntity(
                experiment.getId(),
                experiment.getName(),
                experiment.getDescription(),
                config.getHost(),
                config.getPort(),
                config.getExecutionMode(),
                request.getTotalRequests(),
                request.getMeasurementDurationMs(),
                config.getConcurrency(),
                config.getWarmupDurationMs(),
                config.getRequestTimeoutMs(),
                experiment.getRepetitions(),
                request.getThreadPoolSize(),
                ExperimentStatus.CREATED.name(),
                metadata.getOperatingSystem(),
                metadata.getJavaVersion(),
                metadata.getJavaRuntime(),
                metadata.getProcessor(),
                metadata.getAvailableProcessors(),
                metadata.getMaxMemoryMb(),
                experiment.getCreatedAt()
        );
    }

    // ================================================================
    // ARCHITECTURE PERSISTENCE
    // ================================================================

    private void persistArchitectures(
            String experimentId,
            List<ServerArchitecture> architectures
    ) {

        List<ExperimentArchitectureEntity> entities =
                new ArrayList<>();

        for (ServerArchitecture architecture :
                architectures) {

            entities.add(
                    new ExperimentArchitectureEntity(
                            experimentId,
                            architecture
                    )
            );
        }

        experimentArchitectureRepository.saveAll(
                entities
        );
    }

    // ================================================================
    // DATABASE LOOKUP
    // ================================================================

    private ExperimentEntity getExperimentEntity(
            String experimentId
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        return experimentRepository
                .findById(
                        experimentId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Experiment not found: "
                                                + experimentId
                                )
                );
    }

    private ExperimentRecord
    getOrRestoreRuntimeRecord(
            String experimentId
    ) {

        ExperimentRecord existingRecord =
                experiments.get(
                        experimentId
                );

        if (existingRecord != null) {
            return existingRecord;
        }

        ExperimentEntity entity =
                getExperimentEntity(
                        experimentId
                );

        Experiment experiment =
                restoreExperiment(
                        entity
                );

        ExperimentRequest request =
                new ExperimentRequest();

        request.setName(
                entity.getName()
        );

        request.setDescription(
                entity.getDescription()
        );

        request.setHost(
                entity.getHost()
        );

        request.setPort(
                entity.getPort()
        );

        request.setExecutionMode(
                entity.getExecutionMode()
        );

        request.setTotalRequests(
                entity.getTotalRequests()
        );

        request.setMeasurementDurationMs(
                entity.getMeasurementDurationMs()
        );

        request.setConcurrency(
                entity.getConcurrency()
        );

        request.setWarmupDurationMs(
                entity.getWarmupDurationMs()
        );

        request.setRequestTimeoutMs(
                entity.getRequestTimeoutMs()
        );

        request.setRepetitions(
                entity.getRepetitions()
        );

        request.setThreadPoolSize(
                entity.getThreadPoolSize()
        );

        request.setArchitectures(
                experiment.getArchitectures()
        );

        ExperimentRecord restoredRecord =
                new ExperimentRecord(
                        experiment,
                        request,
                        entity
                );

        restoredRecord.status =
                ExperimentStatus.valueOf(
                        entity.getStatus()
                );

        ExperimentRecord previousRecord =
                experiments.putIfAbsent(
                        experimentId,
                        restoredRecord
                );

        return previousRecord != null
                ? previousRecord
                : restoredRecord;
    }

    private ExperimentRecord getRuntimeRecord(
            String experimentId
    ) {

        ExperimentRecord record =
                experiments.get(
                        experimentId
                );

        if (record == null) {

            throw new IllegalArgumentException(
                    "Experiment not found: "
                            + experimentId
            );
        }

        return record;
    }

    // ================================================================
    // ERROR MESSAGE
    // ================================================================

    private String buildErrorMessage(
            Exception exception
    ) {

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

    // ================================================================
    // STATUS
    // ================================================================

    public enum ExperimentStatus {

        CREATED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // ================================================================
    // RUNTIME RECORD
    // ================================================================

    private static final class ExperimentRecord {

        private final Experiment experiment;
        private final ExperimentRequest request;
        private final ExperimentEntity experimentEntity;

        private volatile ExperimentStatus status;
        private volatile ExperimentResult result;
        private volatile String errorMessage;

        private volatile CompletableFuture<Void>
                executionFuture;

        private ExperimentRecord(
                Experiment experiment,
                ExperimentRequest request,
                ExperimentEntity experimentEntity
        ) {

            this.experiment =
                    experiment;

            this.request =
                    request;

            this.experimentEntity =
                    experimentEntity;

            this.status =
                    ExperimentStatus.CREATED;

            this.errorMessage =
                    "";
        }
    }
}