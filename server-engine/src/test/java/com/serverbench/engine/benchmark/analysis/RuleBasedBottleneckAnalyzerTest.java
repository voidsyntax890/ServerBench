package com.serverbench.engine.benchmark.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.serverbench.engine.benchmark.BenchmarkResult;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.ServerArchitecture;

class RuleBasedBottleneckAnalyzerTest {

    private final RuleBasedBottleneckAnalyzer analyzer =
            new RuleBasedBottleneckAnalyzer();

    @Test
    void shouldRejectNullExperiment() {

        try {

            analyzer.analyze(null);

        } catch (IllegalArgumentException exception) {

            assertEquals(
                    "Experiment result cannot be null.",
                    exception.getMessage()
            );

            return;
        }

        throw new AssertionError(
                "Expected IllegalArgumentException."
        );
    }

    @Test
    void shouldRejectEmptyExperiment() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Empty Experiment"
                );

        try {

            analyzer.analyze(experimentResult);

        } catch (IllegalArgumentException exception) {

            assertEquals(
                    "Experiment contains no run results.",
                    exception.getMessage()
            );

            return;
        }

        throw new AssertionError(
                "Expected IllegalArgumentException."
        );
    }

    @Test
    void shouldDetectAllRunsFailedAsCritical() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Failure Experiment"
                );

        experimentResult.addRunResult(
                failedRun(
                        ServerArchitecture.THREAD_POOL,
                        1,
                        "Server failed"
                )
        );

        BottleneckReport report =
                analyzer.analyze(
                        experimentResult
                );

        assertTrue(
                report.hasFindings()
        );

        BottleneckFinding finding =
                report.getFindings()
                        .stream()
                        .filter(
                                item ->
                                        item.getCategory()
                                                == BottleneckCategory.RUN_STABILITY
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(finding);

        assertEquals(
                BottleneckSeverity.CRITICAL,
                finding.getSeverity()
        );

        assertEquals(
                "THREAD_POOL",
                finding.getArchitecture()
        );
    }

    @Test
    void shouldDetectMixedRunStabilityAsWarning() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Mixed Experiment"
                );

        LocalDateTime start =
                LocalDateTime.of(
                        2026,
                        9,
                        7,
                        10,
                        0
                );

        LocalDateTime finish =
                start.plusSeconds(1);

        experimentResult.addRunResult(
                completedRun(
                        ServerArchitecture.THREAD_POOL,
                        1,
                        createBenchmarkResult(
                                "Thread Pool",
                                100,
                                100,
                                0,
                                1000.0,
                                1.0,
                                1.0,
                                3.0,
                                2.0,
                                2.5,
                                3.0
                        ),
                        start,
                        finish
                )
        );

        experimentResult.addRunResult(
                failedRun(
                        ServerArchitecture.THREAD_POOL,
                        2,
                        "Execution failed"
                )
        );

        BottleneckReport report =
                analyzer.analyze(
                        experimentResult
                );

        assertTrue(
                report.getFindings()
                        .stream()
                        .anyMatch(
                                finding ->
                                        finding.getCategory()
                                                == BottleneckCategory.RUN_STABILITY
                                                && finding.getSeverity()
                                                        == BottleneckSeverity.WARNING
                        )
        );
    }

    @Test
    void shouldDetectReadTimeoutPattern() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Timeout Experiment"
                );

        BenchmarkResult result =
                createBenchmarkResultWithFailures(
                        "Thread Pool",
                        1000,
                        990,
                        10,
                        0,
                        0,
                        5
                );

        experimentResult.addRunResult(
                completedRun(
                        ServerArchitecture.THREAD_POOL,
                        1,
                        result,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusSeconds(1)
                )
        );

        BottleneckReport report =
                analyzer.analyze(
                        experimentResult
                );

        assertTrue(
                report.getFindings()
                        .stream()
                        .anyMatch(
                                finding ->
                                        finding.getCategory()
                                                == BottleneckCategory.TIMEOUT_PATTERN
                                                && finding.getTitle()
                                                        .contains(
                                                                "Read timeout"
                                                        )
                        )
        );
    }

    @Test
    void shouldDetectTailLatencyObservation() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Tail Experiment"
                );

        BenchmarkResult result =
                createBenchmarkResult(
                        "Virtual Thread",
                        100,
                        100,
                        0,
                        1000.0,
                        5.0,
                        1.0,
                        50.0,
                        5.0,
                        20.0,
                        50.0
                );

        experimentResult.addRunResult(
                completedRun(
                        ServerArchitecture.VIRTUAL_THREAD,
                        1,
                        result,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusSeconds(1)
                )
        );

        BottleneckReport report =
                analyzer.analyze(
                        experimentResult
                );

        assertTrue(
                report.getFindings()
                        .stream()
                        .anyMatch(
                                finding ->
                                        finding.getCategory()
                                                == BottleneckCategory.TAIL_LATENCY
                        )
        );
    }

    @Test
    void shouldNotReportTailLatencyWhenP99EqualsP50() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Flat Tail Experiment"
                );

        BenchmarkResult result =
                createBenchmarkResult(
                        "Single Threaded",
                        100,
                        100,
                        0,
                        1000.0,
                        5.0,
                        1.0,
                        5.0,
                        5.0,
                        5.0,
                        5.0
                );

        experimentResult.addRunResult(
                completedRun(
                        ServerArchitecture.SINGLE_THREADED,
                        1,
                        result,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusSeconds(1)
                )
        );

        BottleneckReport report =
                analyzer.analyze(
                        experimentResult
                );

        assertFalse(
                report.getFindings()
                        .stream()
                        .anyMatch(
                                finding ->
                                        finding.getCategory()
                                                == BottleneckCategory.TAIL_LATENCY
                        )
        );
    }

    @Test
    void shouldKeepReportFindingsImmutable() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "Immutable Experiment"
                );

        experimentResult.addRunResult(
                completedRun(
                        ServerArchitecture.SINGLE_THREADED,
                        1,
                        createBenchmarkResult(
                                "Single Threaded",
                                100,
                                100,
                                0,
                                1000.0,
                                1.0,
                                1.0,
                                1.0,
                                1.0,
                                1.0,
                                1.0
                        ),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusSeconds(1)
                )
        );

        BottleneckReport report =
                analyzer.analyze(
                        experimentResult
                );

        try {

            report.getFindings().clear();

        } catch (UnsupportedOperationException expected) {

            return;
        }

        throw new AssertionError(
                "Expected findings list to be immutable."
        );
    }

    private ExperimentRunResult completedRun(
            ServerArchitecture architecture,
            int repetition,
            BenchmarkResult result,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {

        return new ExperimentRunResult(
                architecture,
                repetition,
                result,
                ExperimentRunResult.Status.COMPLETED,
                "",
                startedAt,
                finishedAt
        );
    }

    private ExperimentRunResult failedRun(
            ServerArchitecture architecture,
            int repetition,
            String message
    ) {

        LocalDateTime startedAt =
                LocalDateTime.now();

        return new ExperimentRunResult(
                architecture,
                repetition,
                null,
                ExperimentRunResult.Status.FAILED,
                message,
                startedAt,
                startedAt.plusSeconds(1)
        );
    }

    private BenchmarkResult createBenchmarkResult(
            String serverType,
            int totalRequests,
            int successfulRequests,
            int failedRequests,
            double throughput,
            double averageLatency,
            double minimumLatency,
            double maximumLatency,
            double p50,
            double p95,
            double p99
    ) {

        double successRate =
                totalRequests == 0
                        ? 0.0
                        : (
                                (double) successfulRequests
                                        / totalRequests
                        ) * 100.0;

        double errorRate =
                totalRequests == 0
                        ? 0.0
                        : (
                                (double) failedRequests
                                        / totalRequests
                        ) * 100.0;

        return new BenchmarkResult(
                serverType,
                totalRequests,
                successfulRequests,
                failedRequests,
                1000L,
                averageLatency,
                throughput,
                successRate,
                errorRate,
                (long) minimumLatency,
                (long) maximumLatency,
                p50,
                p95,
                p99,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    private BenchmarkResult createBenchmarkResultWithFailures(
            String serverType,
            int totalRequests,
            int successfulRequests,
            int failedRequests,
            int connectTimeouts,
            int connectionRefused,
            int readTimeouts
    ) {

        return new BenchmarkResult(
                serverType,
                totalRequests,
                successfulRequests,
                failedRequests,
                1000L,
                5.0,
                1000.0,
                (
                        (double) successfulRequests
                                / totalRequests
                ) * 100.0,
                (
                        (double) failedRequests
                                / totalRequests
                ) * 100.0,
                1,
                10,
                5.0,
                20.0,
                50.0,
                connectTimeouts,
                connectionRefused,
                0,
                readTimeouts,
                0,
                0
        );
    }
}