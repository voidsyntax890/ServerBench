package com.serverbench.backend.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.serverbench.engine.benchmark.BenchmarkResult;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.ServerArchitecture;
import com.serverbench.engine.benchmark.analysis.BottleneckCategory;
import com.serverbench.engine.benchmark.analysis.BottleneckFinding;
import com.serverbench.engine.benchmark.analysis.BottleneckSeverity;

class PerformanceExplanationServiceTest {

    @Test
    void shouldBuildPromptUsingFindingAndCompleteExperimentContext() {

        BottleneckFinding finding =
                createFinding();

        ExperimentResult experimentResult =
                createExperimentResult();

        String prompt =
                PerformanceExplanationService.buildPrompt(
                        finding,
                        experimentResult
                );

        assertTrue(
                prompt.contains(
                        "SELECTED FINDING"
                )
        );

        assertTrue(
                prompt.contains(
                        "TAIL_LATENCY"
                )
        );

        assertTrue(
                prompt.contains(
                        "Tail latency exceeds median latency"
                )
        );

        assertTrue(
                prompt.contains(
                        "p50LatencyMs=5.0000"
                )
        );

        assertTrue(
                prompt.contains(
                        "p95LatencyMs=20.0000"
                )
        );

        assertTrue(
                prompt.contains(
                        "p99LatencyMs=50.0000"
                )
        );

        assertTrue(
                prompt.contains(
                        "architecture=VIRTUAL_THREAD"
                )
        );

        assertTrue(
                prompt.contains(
                        "repetition=1"
                )
        );

        assertTrue(
                prompt.contains(
                        "throughputRequestsPerSecond=1000.0000"
                )
        );

        assertTrue(
                prompt.contains(
                        "ANALYSIS REQUIREMENTS"
                )
        );

        assertTrue(
                prompt.contains(
                        "Cite the measured values"
                )
        );

        assertTrue(
                prompt.contains(
                        "repeated runs"
                )
        );

        assertTrue(
                prompt.contains(
                        "Do not claim causation"
                )
        );
    }

    @Test
    void shouldIncludeFailedRunsInExperimentContext() {

        ExperimentResult experimentResult =
                createExperimentResult();

        LocalDateTime startedAt =
                LocalDateTime.of(
                        2026,
                        9,
                        12,
                        18,
                        0
                );

        experimentResult.addRunResult(
                new ExperimentRunResult(
                        ServerArchitecture.THREAD_POOL,
                        2,
                        null,
                        ExperimentRunResult.Status.FAILED,
                        "Connection refused",
                        startedAt,
                        startedAt.plusSeconds(1)
                )
        );

        String prompt =
                PerformanceExplanationService.buildPrompt(
                        createFinding(),
                        experimentResult
                );

        assertTrue(
                prompt.contains(
                        "architecture=THREAD_POOL"
                )
        );

        assertTrue(
                prompt.contains(
                        "status=FAILED"
                )
        );

        assertTrue(
                prompt.contains(
                        "errorMessage=Connection refused"
                )
        );

        assertTrue(
                prompt.contains(
                        "benchmarkResult=unavailable"
                )
        );
    }

    @Test
    void shouldPreserveFindingRecommendationAsAuthoritativeContext() {

        String recommendation =
                "Investigate intermittent blocking.";

        BottleneckFinding finding =
                new BottleneckFinding(
                        BottleneckCategory.TAIL_LATENCY,
                        BottleneckSeverity.INFO,
                        "VIRTUAL_THREAD",
                        "Tail latency exceeds median latency",
                        "The upper latency percentile is higher than the median.",
                        "p50LatencyMs=5.0000, p95LatencyMs=20.0000, p99LatencyMs=50.0000",
                        recommendation
                );

        String prompt =
                PerformanceExplanationService.buildPrompt(
                        finding,
                        createExperimentResult()
                );

        assertTrue(
                prompt.contains(
                        recommendation
                )
        );

        assertEquals(
                1,
                countOccurrences(
                        prompt,
                        recommendation
                )
        );
    }

    private BottleneckFinding createFinding() {

        return new BottleneckFinding(
                BottleneckCategory.TAIL_LATENCY,
                BottleneckSeverity.INFO,
                "VIRTUAL_THREAD",
                "Tail latency exceeds median latency",
                "The upper latency percentile is higher than the median.",
                "p50LatencyMs=5.0000, p95LatencyMs=20.0000, p99LatencyMs=50.0000",
                "Investigate intermittent blocking."
        );
    }

    private ExperimentResult createExperimentResult() {

        ExperimentResult experimentResult =
                new ExperimentResult(
                        "experiment-1",
                        "AI Context Experiment"
                );

        LocalDateTime startedAt =
                LocalDateTime.of(
                        2026,
                        9,
                        12,
                        18,
                        0
                );

        experimentResult.addRunResult(
                new ExperimentRunResult(
                        ServerArchitecture.VIRTUAL_THREAD,
                        1,
                        new BenchmarkResult(
                                "Virtual Thread",
                                1000,
                                1000,
                                0,
                                1000L,
                                5.0000,
                                1000.0000,
                                100.0,
                                0.0,
                                1L,
                                50L,
                                5.0000,
                                20.0000,
                                50.0000,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0
                        ),
                        ExperimentRunResult.Status.COMPLETED,
                        "",
                        startedAt,
                        startedAt.plusSeconds(1)
                )
        );

        return experimentResult;
    }

    private int countOccurrences(
            String text,
            String value
    ) {

        int count = 0;
        int index = 0;

        while ((index =
                text.indexOf(value, index)) >= 0) {

            count++;
            index += value.length();
        }

        return count;
    }
}