package com.serverbench.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.serverbench.backend.dto.request.ExperimentRequest;
import com.serverbench.engine.benchmark.BenchmarkConfig;
import com.serverbench.engine.benchmark.ComparisonSummary;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ExperimentAnalyzer;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunner;
import com.serverbench.engine.benchmark.ServerArchitecture;
import com.serverbench.engine.benchmark.ServerFactory;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

@Service
public class ExperimentService {

    private static final int DEFAULT_THREAD_POOL_SIZE = 1;

    /*
     * Temporary M4 storage.
     *
     * PostgreSQL will replace this storage layer in M5.
     */
    private final Map<String, ExperimentRecord> experiments
            = new ConcurrentHashMap<>();

    /**
     * Creates and stores a ServerBench experiment from the validated API
     * request.
     *
     * Creation does not start benchmark execution.
     */
    public Experiment createExperiment(
            ExperimentRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Experiment request cannot be null."
            );
        }

        BenchmarkConfig benchmarkConfig
                = new BenchmarkConfig(
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

        Experiment experiment
                = new Experiment(
                        request.getName(),
                        request.getDescription(),
                        benchmarkConfig,
                        request.getArchitectures(),
                        request.getRepetitions()
                );

        /*
         * Store both the Experiment and the original request.
         *
         * The original request is needed later for
         * architecture-specific configuration such as
         * Thread Pool size.
         */
        ExperimentRecord record
                = new ExperimentRecord(
                        experiment,
                        request
                );

        experiments.put(
                experiment.getId(),
                record
        );

        return experiment;
    }

    /**
     * Starts an existing experiment asynchronously.
     */
    public void startExperiment(
            String experimentId
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        ExperimentRecord record
                = experiments.get(
                        experimentId
                );

        if (record == null) {

            throw new IllegalArgumentException(
                    "Experiment not found: "
                    + experimentId
            );
        }

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

            record.status
                    = ExperimentStatus.RUNNING;

            record.executionFuture
                    = CompletableFuture.runAsync(
                            () -> executeExperiment(
                                    record
                            )
                    );
        }
    }

    /**
     * Returns the stored experiment.
     */
    public Experiment getExperiment(
            String experimentId
    ) {

        return getRecord(
                experimentId
        ).experiment;
    }

    /**
     * Returns the current execution status.
     */
    public ExperimentStatus getStatus(
            String experimentId
    ) {

        return getRecord(
                experimentId
        ).status;
    }

    /**
     * Returns the completed experiment result.
     *
     * Returns null while execution is still running or before execution has
     * started.
     */
    public ExperimentResult getResult(
            String experimentId
    ) {

        return getRecord(
                experimentId
        ).result;
    }

    /**
     * Returns the execution error, if the experiment failed.
     */
    public String getErrorMessage(
            String experimentId
    ) {

        return getRecord(
                experimentId
        ).errorMessage;
    }

    /**
     * Returns all stored experiments in creation order, newest first.
     */
    public List<Experiment> getAllExperiments() {

        List<Experiment> allExperiments
                = new ArrayList<>();

        for (ExperimentRecord record
                : experiments.values()) {

            allExperiments.add(
                    record.experiment
            );
        }

        allExperiments.sort(
                Comparator.comparing(
                        Experiment::getCreatedAt
                ).reversed()
        );

        return List.copyOf(
                allExperiments
        );
    }

    /**
     * Returns the Thread Pool size configured for an experiment.
     *
     * The value is only meaningful when THREAD_POOL was selected.
     */
    public Integer getThreadPoolSize(
            String experimentId
    ) {

        return getRecord(
                experimentId
        ).request.getThreadPoolSize();
    }

    /**
     * Creates a comparison summary from the completed experiment result using
     * the existing engine analyzer.
     */
    public ComparisonSummary getComparisonSummary(
            String experimentId
    ) {

        ExperimentResult result
                = getRecord(
                        experimentId
                ).result;

        if (result == null) {

            throw new IllegalStateException(
                    "Experiment results are not available yet."
            );
        }

        ExperimentAnalyzer analyzer
                = new ExperimentAnalyzer();

        return analyzer.analyze(
                result
        );
    }

    /**
     * Executes the experiment in the background.
     */
    private void executeExperiment(
            ExperimentRecord record
    ) {

        try {

            Experiment experiment
                    = record.experiment;

            ExperimentRequest request
                    = record.request;

            if (request == null) {

                throw new IllegalStateException(
                        "Stored experiment request is missing."
                );
            }

            int threadPoolSize
                    = determineThreadPoolSize(
                            request
                    );

            ServerConfig serverConfig
                    = new ServerConfig(
                            experiment
                                    .getBenchmarkConfig()
                                    .getPort(),
                            threadPoolSize,
                            false
                    );

            Map<
                    ServerArchitecture, Supplier<ServerEngine>> serverFactories
                    = ServerFactory.createFactories(
                            serverConfig
                    );

            ExperimentRunner experimentRunner
                    = new ExperimentRunner(
                            experiment,
                            serverFactories
                    );

            ExperimentResult result
                    = experimentRunner.run();

            synchronized (record) {

                record.result
                        = result;

                record.status
                        = ExperimentStatus.COMPLETED;
            }

        } catch (Exception exception) {

            synchronized (record) {

                record.errorMessage
                        = buildErrorMessage(
                                exception
                        );

                record.status
                        = ExperimentStatus.FAILED;
            }
        }
    }

    /**
     * Determines the Thread Pool size.
     *
     * Thread Pool configuration is meaningful only when THREAD_POOL is
     * selected.
     */
    private int determineThreadPoolSize(
            ExperimentRequest request
    ) {

        List<ServerArchitecture> architectures
                = request.getArchitectures();

        if (architectures.contains(
                ServerArchitecture.THREAD_POOL
        )) {

            Integer threadPoolSize
                    = request.getThreadPoolSize();

            if (threadPoolSize == null
                    || threadPoolSize <= 0) {

                throw new IllegalArgumentException(
                        "Thread pool size must be greater than 0 "
                        + "when THREAD_POOL is selected."
                );
            }

            return threadPoolSize;
        }

        /*
         * ServerConfig requires a positive pool size even
         * when ThreadPoolServer is not being tested.
         */
        return DEFAULT_THREAD_POOL_SIZE;
    }

    /**
     * Finds an experiment in the temporary M4 store.
     */
    private ExperimentRecord getRecord(
            String experimentId
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        ExperimentRecord record
                = experiments.get(
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

    private String buildErrorMessage(
            Exception exception
    ) {

        String message
                = exception.getMessage();

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

    /**
     * Returns all server architectures supported by ServerBench.
     */
    public List<ServerArchitecture> getAvailableArchitectures() {

        return List.of(
                ServerArchitecture.SINGLE_THREADED,
                ServerArchitecture.MULTI_THREADED,
                ServerArchitecture.THREAD_POOL,
                ServerArchitecture.VIRTUAL_THREAD
        );
    }

    //experiment status section
    public enum ExperimentStatus {

        CREATED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    private static final class ExperimentRecord {

        private final Experiment experiment;
        private final ExperimentRequest request;

        private volatile ExperimentStatus status;
        private volatile ExperimentResult result;
        private volatile String errorMessage;

        private volatile CompletableFuture<Void> executionFuture;

        private ExperimentRecord(
                Experiment experiment,
                ExperimentRequest request
        ) {

            this.experiment
                    = experiment;

            this.request
                    = request;

            this.status
                    = ExperimentStatus.CREATED;

            this.errorMessage
                    = "";
        }
    }
}
