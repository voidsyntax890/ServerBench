package com.serverbench.backend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.serverbench.backend.dto.request.ExperimentRequest;
import com.serverbench.engine.benchmark.BenchmarkConfig;
import com.serverbench.engine.benchmark.Experiment;
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
     * Creates and stores a ServerBench experiment from
     * the validated API request.
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

        /*
         * IMPORTANT:
         * Store both the Experiment and the original request.
         *
         * The request is needed later when the experiment starts
         * so that architecture-specific settings such as the
         * Thread Pool size can be reconstructed.
         */
        ExperimentRecord record =
                new ExperimentRecord(
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
     *
     * The HTTP request does not remain blocked for the
     * entire duration of the benchmark.
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

            record.executionFuture =
                    CompletableFuture.runAsync(
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
     * Returns null while execution is still running or
     * before execution has started.
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
     * Executes the experiment in the background.
     */
    private void executeExperiment(
            ExperimentRecord record
    ) {

        try {

            Experiment experiment =
                    record.experiment;

            ExperimentRequest request =
                    record.request;

            /*
             * This should never be null because createExperiment()
             * stores the original request together with the experiment.
             */
            if (request == null) {

                throw new IllegalStateException(
                        "Stored experiment request is missing."
                );
            }

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

            ExperimentRunner experimentRunner =
                    new ExperimentRunner(
                            experiment,
                            serverFactories
                    );

            ExperimentResult result =
                    experimentRunner.run();

            synchronized (record) {

                record.result =
                        result;

                record.status =
                        ExperimentStatus.COMPLETED;
            }

        } catch (Exception exception) {

            synchronized (record) {

                record.errorMessage =
                        buildErrorMessage(
                                exception
                        );

                record.status =
                        ExperimentStatus.FAILED;
            }
        }
    }

    /**
     * Determines the Thread Pool size.
     *
     * Thread Pool configuration is meaningful only when
     * THREAD_POOL is selected.
     */
    private int determineThreadPoolSize(
            ExperimentRequest request
    ) {

        List<ServerArchitecture> architectures =
                request.getArchitectures();

        if (architectures.contains(
                ServerArchitecture.THREAD_POOL
        )) {

            Integer threadPoolSize =
                    request.getThreadPoolSize();

            /*
             * Validation should already have guaranteed this,
             * but keep a defensive check at the engine boundary.
             */
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
         * ServerConfig requires a positive pool size even when
         * ThreadPoolServer is not part of the experiment.
         *
         * This value has no effect on the other architectures.
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
    // EXPERIMENT STATUS
    // ================================================================

    public enum ExperimentStatus {

        CREATED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // ================================================================
    // INTERNAL EXPERIMENT RECORD
    // ================================================================

    private static final class ExperimentRecord {

        private final Experiment experiment;
        private final ExperimentRequest request;

        private volatile ExperimentStatus status;
        private volatile ExperimentResult result;
        private volatile String errorMessage;

        private volatile CompletableFuture<Void>
                executionFuture;

        private ExperimentRecord(
                Experiment experiment,
                ExperimentRequest request
        ) {

            this.experiment =
                    experiment;

            this.request =
                    request;

            this.status =
                    ExperimentStatus.CREATED;

            this.errorMessage =
                    "";
        }
    }
}