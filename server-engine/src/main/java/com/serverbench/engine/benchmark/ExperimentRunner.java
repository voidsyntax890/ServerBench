package com.serverbench.engine.benchmark;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.serverbench.engine.core.ServerEngine;

public class ExperimentRunner {

    /*
     * Startup timeout is an execution safety limit.
     * It is intentionally separate from the user's
     * request timeout.
     */
    private static final long SERVER_STARTUP_TIMEOUT_MS =
            10_000L;

    private static final long SERVER_STOP_TIMEOUT_MS =
            2_000L;

    private static final long SERVER_READINESS_POLL_MS =
            100L;

    private final Experiment experiment;

    private final Map<
            ServerArchitecture,
            Supplier<ServerEngine>
            > serverFactories;

    private final Consumer<ExperimentProgress>
            progressListener;

    public ExperimentRunner(
            Experiment experiment,
            Map<
                    ServerArchitecture,
                    Supplier<ServerEngine>
                    > serverFactories
    ) {

        this(
                experiment,
                serverFactories,
                null
        );
    }

    public ExperimentRunner(
            Experiment experiment,
            Map<
                    ServerArchitecture,
                    Supplier<ServerEngine>
                    > serverFactories,
            Consumer<ExperimentProgress>
                    progressListener
    ) {

        if (experiment == null) {

            throw new IllegalArgumentException(
                    "Experiment cannot be null."
            );
        }

        if (serverFactories == null) {

            throw new IllegalArgumentException(
                    "Server factories cannot be null."
            );
        }

        validateServerFactories(
                experiment,
                serverFactories
        );

        this.experiment =
                experiment;

        this.serverFactories =
                serverFactories;

        this.progressListener =
                progressListener;
    }

    public ExperimentResult run() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        experiment.getId(),
                        experiment.getName()
                );

        int totalRuns =
                experiment.getArchitectures().size()
                        * experiment.getRepetitions();

        int completedRuns = 0;

        for (
                ServerArchitecture architecture :
                experiment.getArchitectures()
        ) {

            for (
                    int repetition = 1;
                    repetition <= experiment.getRepetitions();
                    repetition++
            ) {

                notifyProgress(
                        new ExperimentProgress(
                                architecture,
                                repetition,
                                completedRuns,
                                totalRuns
                        )
                );

                ExperimentRunResult runResult =
                        executeSingleRun(
                                architecture,
                                repetition
                        );

                experimentResult.addRunResult(
                        runResult
                );

                completedRuns++;

                notifyProgress(
                        new ExperimentProgress(
                                architecture,
                                repetition,
                                completedRuns,
                                totalRuns
                        )
                );
            }
        }

        return experimentResult;
    }

    private void notifyProgress(
            ExperimentProgress progress
    ) {

        if (progressListener == null) {
            return;
        }

        try {
            progressListener.accept(
                    progress
            );
        } catch (RuntimeException ignored) {
            /*
             * Progress reporting must never
             * affect benchmark execution.
             */
        }
    }

    private ExperimentRunResult executeSingleRun(
            ServerArchitecture architecture,
            int repetitionNumber
    ) {

        LocalDateTime startedAt =
                LocalDateTime.now();

        ServerEngine server = null;

        Thread serverThread = null;

        try {

            Supplier<ServerEngine> factory =
                    serverFactories.get(architecture);

            /*
             * A fresh server instance is required for every
             * repetition. This is especially important for
             * ThreadPoolServer because its executor is shut
             * down when the server is stopped.
             */
            server = factory.get();

            if (server == null) {

                throw new IllegalStateException(
                        "Server factory returned null for "
                                + architecture
                );
            }

            ServerEngine finalServer = server;

            AtomicReference<Throwable> startupFailure =
                    new AtomicReference<>();

            serverThread =
                    new Thread(
                            () -> {

                                try {

                                    finalServer.start();

                                } catch (Throwable throwable) {

                                    startupFailure
                                            .set(throwable);
                                }
                            },
                            "ServerBench-"
                                    + architecture
                                    + "-Run-"
                                    + repetitionNumber
                    );

            serverThread.start();

            waitForServer(
                    startupFailure
            );

            BenchmarkRunner benchmarkRunner =
                    new BenchmarkRunner(
                            experiment.getBenchmarkConfig(),
                            server.getServerType()
                    );

            BenchmarkResult benchmarkResult =
                    benchmarkRunner.run();

            LocalDateTime finishedAt =
                    LocalDateTime.now();

            return new ExperimentRunResult(
                    architecture,
                    repetitionNumber,
                    benchmarkResult,
                    ExperimentRunResult.Status.COMPLETED,
                    "",
                    startedAt,
                    finishedAt
            );

        } catch (Exception e) {

            LocalDateTime finishedAt =
                    LocalDateTime.now();

            return new ExperimentRunResult(
                    architecture,
                    repetitionNumber,
                    null,
                    ExperimentRunResult.Status.FAILED,
                    buildErrorMessage(e),
                    startedAt,
                    finishedAt
            );

        } finally {

            if (server != null) {

                try {

                    server.stop();

                } catch (Exception ignored) {
                    /*
                     * We still try to join the server thread.
                     * Cleanup problems are not allowed to prevent
                     * the next experiment run from being attempted.
                     */
                }
            }

            if (serverThread != null) {

                try {

                    serverThread.join(
                            SERVER_STOP_TIMEOUT_MS
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void waitForServer(
            AtomicReference<Throwable> startupFailure
    ) throws Exception {

        long startTime =
                System.currentTimeMillis();

        while (
                System.currentTimeMillis()
                        - startTime
                        < SERVER_STARTUP_TIMEOUT_MS
        ) {

            Throwable failure =
                    startupFailure.get();

            if (failure != null) {

                throw new IllegalStateException(
                        "Server failed during startup.",
                        failure
                );
            }

            try (
                    Socket socket =
                            new Socket(
                                    experiment
                                            .getBenchmarkConfig()
                                            .getHost(),
                                    experiment
                                            .getBenchmarkConfig()
                                            .getPort()
                            )
            ) {

                return;

            } catch (IOException ignored) {

                try {

                    Thread.sleep(
                            SERVER_READINESS_POLL_MS
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    throw new IllegalStateException(
                            "Interrupted while waiting "
                                    + "for server readiness."
                    );
                }
            }
        }

        Throwable failure =
                startupFailure.get();

        if (failure != null) {

            throw new IllegalStateException(
                    "Server failed during startup.",
                    failure
            );
        }

        throw new IllegalStateException(
                "Server did not become ready on "
                        + experiment
                        .getBenchmarkConfig()
                        .getHost()
                        + ":"
                        + experiment
                        .getBenchmarkConfig()
                        .getPort()
                        + " within "
                        + SERVER_STARTUP_TIMEOUT_MS
                        + " ms."
        );
    }

    private void validateServerFactories(
            Experiment experiment,
            Map<
                    ServerArchitecture,
                    Supplier<ServerEngine>
                    > serverFactories
    ) {

        Set<ServerArchitecture> selectedArchitectures =
                new HashSet<>(
                        experiment.getArchitectures()
                );

        for (ServerArchitecture architecture :
                selectedArchitectures) {

            if (!serverFactories.containsKey(
                    architecture
            )) {

                throw new IllegalArgumentException(
                        "No server factory provided for "
                                + architecture
                );
            }

            if (serverFactories.get(architecture)
                    == null) {

                throw new IllegalArgumentException(
                        "Server factory cannot be null for "
                                + architecture
                );
            }
        }

        /*
         * Prevent accidental duplicate architecture
         * selections from silently producing duplicate
         * groups of runs.
         */
        if (selectedArchitectures.size()
                != experiment.getArchitectures().size()) {

            throw new IllegalArgumentException(
                    "Experiment contains duplicate "
                            + "server architectures."
            );
        }
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
}