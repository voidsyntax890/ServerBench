package com.serverbench.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.serverbench.engine.benchmark.BenchmarkConfig;
import com.serverbench.engine.benchmark.ComparisonSummary;
import com.serverbench.engine.benchmark.EnvironmentMetadata;
import com.serverbench.engine.benchmark.ExecutionMode;
import com.serverbench.engine.benchmark.Experiment;
import com.serverbench.engine.benchmark.ExperimentAnalyzer;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.ExperimentRunner;
import com.serverbench.engine.benchmark.ServerArchitecture;
import com.serverbench.engine.benchmark.ServerFactory;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

public class Main {

    public static void main(String[] args) {

        // ============================================================
        // SERVER CONFIGURATION
        // ============================================================

        ServerConfig serverConfig =
                new ServerConfig(
                        8010,
                        10,
                        false
                );

        // ============================================================
        // BENCHMARK CONFIGURATION
        //
        // Temporary standalone configuration for M3 testing.
        // Later these values will come from the user.
        // ============================================================

        BenchmarkConfig benchmarkConfig =
                new BenchmarkConfig(
                        "localhost",
                        8010,
                        100,
                        5,
                        2000,
                        5000,
                        2000,
                        ExecutionMode.DURATION
                );

        // ============================================================
        // EXPERIMENT DEFINITION
        // ============================================================

        List<ServerArchitecture> architectures =
                Arrays.asList(
                        ServerArchitecture.THREAD_POOL,
                        ServerArchitecture.VIRTUAL_THREAD
                );

        int repetitions = 2;

        Experiment experiment =
                new Experiment(
                        "M3 Experiment Test",
                        "End-to-end experiment, repetition and "
                                + "comparison test.",
                        benchmarkConfig,
                        architectures,
                        repetitions
                );

        // ============================================================
        // DISPLAY EXPERIMENT
        // ============================================================

        printExperimentDetails(experiment);

        // ============================================================
        // SERVER FACTORIES
        // ============================================================

        Map<
                ServerArchitecture,
                Supplier<ServerEngine>
                > serverFactories =
                ServerFactory.createFactories(
                        serverConfig
                );

        // ============================================================
        // RUN EXPERIMENT
        // ============================================================

        ExperimentRunner experimentRunner =
                new ExperimentRunner(
                        experiment,
                        serverFactories
                );

        ExperimentResult experimentResult =
                experimentRunner.run();

        // ============================================================
        // DISPLAY INDIVIDUAL RUNS
        // ============================================================

        printExperimentResults(
                experimentResult
        );

        // ============================================================
        // ANALYZE EXPERIMENT
        // ============================================================

        ExperimentAnalyzer analyzer =
                new ExperimentAnalyzer();

        ComparisonSummary comparisonSummary =
                analyzer.analyze(
                        experimentResult
                );

        // ============================================================
        // DISPLAY COMPARISON
        // ============================================================

        comparisonSummary.printSummary();

        System.out.println();
        System.out.println(
                "M3 experiment workflow completed successfully."
        );
    }

    // ================================================================
    // EXPERIMENT DETAILS
    // ================================================================

    private static void printExperimentDetails(
            Experiment experiment
    ) {

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "        SERVERBENCH EXPERIMENT"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Experiment ID: "
                        + experiment.getId()
        );

        System.out.println(
                "Experiment Name: "
                        + experiment.getName()
        );

        System.out.println(
                "Description: "
                        + experiment.getDescription()
        );

        System.out.println(
                "Execution Mode: "
                        + experiment
                        .getBenchmarkConfig()
                        .getExecutionMode()
        );

        System.out.println(
                "Concurrency: "
                        + experiment
                        .getBenchmarkConfig()
                        .getConcurrency()
        );

        System.out.println(
                "Warm-up: "
                        + experiment
                        .getBenchmarkConfig()
                        .getWarmupDurationMs()
                        + " ms"
        );

        System.out.println(
                "Measurement Duration: "
                        + experiment
                        .getBenchmarkConfig()
                        .getMeasurementDurationMs()
                        + " ms"
        );

        System.out.println(
                "Request Timeout: "
                        + experiment
                        .getBenchmarkConfig()
                        .getRequestTimeoutMs()
                        + " ms"
        );

        System.out.println(
                "Repetitions: "
                        + experiment.getRepetitions()
        );

        System.out.println(
                "Architectures: "
                        + experiment.getArchitectures()
        );

        EnvironmentMetadata environment =
                experiment.getEnvironmentMetadata();

        System.out.println(
                "Operating System: "
                        + environment.getOperatingSystem()
        );

        System.out.println(
                "Java Version: "
                        + environment.getJavaVersion()
        );

        System.out.println(
                "Available Processors: "
                        + environment
                        .getAvailableProcessors()
        );

        System.out.println(
                "Max JVM Memory: "
                        + environment.getMaxMemoryMb()
                        + " MB"
        );

        System.out.println(
                "========================================"
        );
    }

    // ================================================================
    // INDIVIDUAL EXPERIMENT RESULTS
    // ================================================================

    private static void printExperimentResults(
            ExperimentResult experimentResult
    ) {

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "       EXPERIMENT RESULTS"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Experiment ID: "
                        + experimentResult
                        .getExperimentId()
        );

        System.out.println(
                "Experiment Name: "
                        + experimentResult
                        .getExperimentName()
        );

        System.out.println(
                "Total Runs: "
                        + experimentResult.getRunCount()
        );

        System.out.println(
                "Successful Runs: "
                        + experimentResult
                        .getSuccessfulRunCount()
        );

        System.out.println(
                "Failed Runs: "
                        + experimentResult.getFailedRunCount()
        );

        System.out.println(
                "========================================"
        );

        for (
                ExperimentRunResult runResult :
                experimentResult.getRunResults()
        ) {

            System.out.println();

            System.out.println(
                    "Architecture: "
                            + runResult
                            .getArchitecture()
            );

            System.out.println(
                    "Repetition: "
                            + runResult
                            .getRepetitionNumber()
            );

            System.out.println(
                    "Status: "
                            + runResult.getStatus()
            );

            System.out.println(
                    "Started: "
                            + runResult.getStartedAt()
            );

            System.out.println(
                    "Finished: "
                            + runResult.getFinishedAt()
            );

            if (runResult.isSuccessful()) {

                runResult
                        .getBenchmarkResult()
                        .printResult();

            } else {

                System.out.println(
                        "Error: "
                                + runResult
                                .getErrorMessage()
                );
            }
        }

        System.out.println();
        System.out.println(
                "========================================"
        );
    }
}