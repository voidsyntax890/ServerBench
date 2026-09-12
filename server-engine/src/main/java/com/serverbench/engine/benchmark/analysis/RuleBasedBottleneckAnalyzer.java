package com.serverbench.engine.benchmark.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.serverbench.engine.benchmark.ArchitectureComparison;
import com.serverbench.engine.benchmark.BenchmarkResult;
import com.serverbench.engine.benchmark.ComparisonSummary;
import com.serverbench.engine.benchmark.ExperimentResult;
import com.serverbench.engine.benchmark.ExperimentRunResult;
import com.serverbench.engine.benchmark.ServerArchitecture;

public class RuleBasedBottleneckAnalyzer {

    public BottleneckReport analyze(
            ExperimentResult experimentResult
    ) {

        if (experimentResult == null) {
            throw new IllegalArgumentException(
                    "Experiment result cannot be null."
            );
        }

        if (experimentResult.getRunResults().isEmpty()) {
            throw new IllegalArgumentException(
                    "Experiment contains no run results."
            );
        }

        List<BottleneckFinding> findings =
                new ArrayList<>();

        Map<
                ServerArchitecture,
                List<ExperimentRunResult>
                > runsByArchitecture =
                groupRunsByArchitecture(
                        experimentResult
                );

        analyzeRunStability(
                runsByArchitecture,
                findings
        );

        analyzeBenchmarkResults(
                runsByArchitecture,
                findings
        );

        analyzeArchitectureComparison(
                experimentResult,
                findings
        );

        return new BottleneckReport(
                experimentResult.getExperimentId(),
                experimentResult.getExperimentName(),
                findings
        );
    }

    private Map<
            ServerArchitecture,
            List<ExperimentRunResult>
            > groupRunsByArchitecture(
            ExperimentResult experimentResult
    ) {

        Map<
                ServerArchitecture,
                List<ExperimentRunResult>
                > grouped =
                new EnumMap<>(
                        ServerArchitecture.class
                );

        for (ExperimentRunResult run :
                experimentResult.getRunResults()) {

            if (run == null) {
                continue;
            }

            grouped
                    .computeIfAbsent(
                            run.getArchitecture(),
                            key -> new ArrayList<>()
                    )
                    .add(run);
        }

        return grouped;
    }

    private void analyzeRunStability(
            Map<
                    ServerArchitecture,
                    List<ExperimentRunResult>
                    > runsByArchitecture,
            List<BottleneckFinding> findings
    ) {

        for (Map.Entry<
                ServerArchitecture,
                List<ExperimentRunResult>
                > entry :
                runsByArchitecture.entrySet()) {

            ServerArchitecture architecture =
                    entry.getKey();

            List<ExperimentRunResult> runs =
                    entry.getValue();

            int totalRuns =
                    runs.size();

            int successfulRuns =
                    (int) runs.stream()
                            .filter(
                                    ExperimentRunResult::isSuccessful
                            )
                            .count();

            int failedRuns =
                    totalRuns - successfulRuns;

            if (failedRuns == totalRuns) {

                findings.add(
                        createFinding(
                                BottleneckCategory.RUN_STABILITY,
                                BottleneckSeverity.CRITICAL,
                                architecture,
                                "All runs failed",
                                "Every recorded benchmark run for this "
                                        + "architecture failed before producing "
                                        + "a benchmark result.",
                                "totalRuns=" + totalRuns
                                        + ", successfulRuns="
                                        + successfulRuns
                                        + ", failedRuns="
                                        + failedRuns,
                                "Inspect the run failure messages and server "
                                        + "startup or execution conditions before "
                                        + "using this architecture for performance "
                                        + "comparison."
                        )
                );

            } else if (failedRuns > 0) {

                findings.add(
                        createFinding(
                                BottleneckCategory.RUN_STABILITY,
                                BottleneckSeverity.WARNING,
                                architecture,
                                "Mixed successful and failed runs",
                                "The architecture did not produce a stable "
                                        + "outcome across its recorded repetitions.",
                                "totalRuns=" + totalRuns
                                        + ", successfulRuns="
                                        + successfulRuns
                                        + ", failedRuns="
                                        + failedRuns,
                                "Review the failure messages and repeat the same "
                                        + "workload before drawing a performance "
                                        + "conclusion."
                        )
                );
            }
        }
    }

    private void analyzeBenchmarkResults(
            Map<
                    ServerArchitecture,
                    List<ExperimentRunResult>
                    > runsByArchitecture,
            List<BottleneckFinding> findings
    ) {

        for (Map.Entry<
                ServerArchitecture,
                List<ExperimentRunResult>
                > entry :
                runsByArchitecture.entrySet()) {

            ServerArchitecture architecture =
                    entry.getKey();

            for (ExperimentRunResult run :
                    entry.getValue()) {

                if (!run.isSuccessful()) {
                    continue;
                }

                BenchmarkResult result =
                        run.getBenchmarkResult();

                if (result == null) {
                    continue;
                }

                analyzeFailurePatterns(
                        architecture,
                        run,
                        result,
                        findings
                );

                analyzeTailLatency(
                        architecture,
                        run,
                        result,
                        findings
                );
            }
        }
    }

    private void analyzeFailurePatterns(
            ServerArchitecture architecture,
            ExperimentRunResult run,
            BenchmarkResult result,
            List<BottleneckFinding> findings
    ) {

        int attempted =
                result.getTotalRequests();

        int failed =
                result.getFailedRequests();

        if (attempted <= 0 || failed <= 0) {
            return;
        }

        String prefix =
                "repetition="
                        + run.getRepetitionNumber()
                        + ", attemptedRequests="
                        + attempted
                        + ", failedRequests="
                        + failed
                        + ", errorRate="
                        + format(
                                result.getErrorRate()
                        )
                        + "%";

        if (result.getReadTimeouts() > 0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.TIMEOUT_PATTERN,
                            failureSeverity(result),
                            architecture,
                            "Read timeout pattern detected",
                            "The benchmark recorded one or more read "
                                    + "timeouts during this run.",
                            prefix
                                    + ", readTimeouts="
                                    + result.getReadTimeouts(),
                            "Review server response behavior and the configured "
                                    + "request timeout under the same workload."
                    )
            );
        }

        if (result.getConnectTimeouts() > 0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.TIMEOUT_PATTERN,
                            failureSeverity(result),
                            architecture,
                            "Connection timeout pattern detected",
                            "The benchmark recorded one or more connection "
                                    + "timeouts during this run.",
                            prefix
                                    + ", connectTimeouts="
                                    + result.getConnectTimeouts(),
                            "Review server availability, connection establishment "
                                    + "time, and the configured request timeout."
                    )
            );
        }

        if (result.getConnectionRefused() > 0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.ERROR_PATTERN,
                            failureSeverity(result),
                            architecture,
                            "Connection refusal pattern detected",
                            "One or more benchmark requests could not establish "
                                    + "a connection to the target server.",
                            prefix
                                    + ", connectionRefused="
                                    + result.getConnectionRefused(),
                            "Inspect server lifecycle and availability for the "
                                    + "affected run before interpreting performance."
                    )
            );
        }

        if (result.getConnectionResets() > 0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.ERROR_PATTERN,
                            failureSeverity(result),
                            architecture,
                            "Connection reset pattern detected",
                            "One or more benchmark connections were reset "
                                    + "during the run.",
                            prefix
                                    + ", connectionResets="
                                    + result.getConnectionResets(),
                            "Inspect server connection handling and determine why "
                                    + "connections are being reset under the workload."
                    )
            );
        }

        if (result.getNoResponseFailures() > 0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.ERROR_PATTERN,
                            failureSeverity(result),
                            architecture,
                            "No-response failure pattern detected",
                            "The benchmark recorded requests for which no response "
                                    + "was received.",
                            prefix
                                    + ", noResponseFailures="
                                    + result.getNoResponseFailures(),
                            "Investigate request completion and server response "
                                    + "handling under the same benchmark configuration."
                    )
            );
        }

        if (result.getOtherIoFailures() > 0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.ERROR_PATTERN,
                            failureSeverity(result),
                            architecture,
                            "Other I/O failure pattern detected",
                            "The benchmark recorded I/O failures that were not "
                                    + "classified into a more specific failure type.",
                            prefix
                                    + ", otherIoFailures="
                                    + result.getOtherIoFailures(),
                            "Inspect the associated benchmark diagnostics and "
                                    + "repeat the run before interpreting the result."
                    )
            );
        }
    }

    private BottleneckSeverity failureSeverity(
            BenchmarkResult result
    ) {

        if (result.getSuccessfulRequests() == 0
                && result.getFailedRequests() > 0) {

            return BottleneckSeverity.CRITICAL;
        }

        return BottleneckSeverity.WARNING;
    }

    private void analyzeTailLatency(
            ServerArchitecture architecture,
            ExperimentRunResult run,
            BenchmarkResult result,
            List<BottleneckFinding> findings
    ) {

        if (result.getSuccessfulRequests() <= 0) {
            return;
        }

        double average =
                result.getAverageLatencyMs();

        double p50 =
                result.getP50LatencyMs();

        double p95 =
                result.getP95LatencyMs();

        double p99 =
                result.getP99LatencyMs();

        if (p99 <= p50) {
            return;
        }

        findings.add(
                createFinding(
                        BottleneckCategory.TAIL_LATENCY,
                        BottleneckSeverity.INFO,
                        architecture,
                        "Tail latency exceeds median latency",
                        "The measured upper latency percentile is higher "
                                + "than the median, indicating that some successful "
                                + "requests took materially longer than the central "
                                + "latency observation.",
                        "repetition="
                                + run.getRepetitionNumber()
                                + ", averageLatencyMs="
                                + format(average)
                                + ", p50LatencyMs="
                                + format(p50)
                                + ", p95LatencyMs="
                                + format(p95)
                                + ", p99LatencyMs="
                                + format(p99),
                        buildTailLatencyRecommendation(
                                p50,
                                p95,
                                p99
                        )
                )
        );
    }

    private String buildTailLatencyRecommendation(
            double p50,
            double p95,
            double p99
    ) {

        if (p50 <= 0.0) {

            return "The median latency is at or below 0 ms while the "
                    + "upper percentiles are higher. Use a longer or higher-volume "
                    + "run to improve latency resolution, then compare p95 and p99 "
                    + "across repeated runs before attributing the tail to the "
                    + "server architecture.";
        }

        double medianToP95 =
                p95 - p50;

        double p95ToP99 =
                p99 - p95;

        double p99AboveMedianPercent =
                ((p99 - p50) / p50) * 100.0;

        if (p95ToP99 > medianToP95) {

            return "The rarest requests contribute more tail expansion than "
                    + "the p50-to-p95 range (p50="
                    + format(p50)
                    + " ms, p95="
                    + format(p95)
                    + " ms, p99="
                    + format(p99)
                    + " ms; p99 is "
                    + format(p99AboveMedianPercent)
                    + "% above p50). Investigate intermittent blocking, "
                    + "connection handling, scheduling pauses, or resource "
                    + "contention, and verify whether the same p99 pattern "
                    + "persists across repeated runs.";
        }

        if (medianToP95 > 0.0) {

            return "Latency rises substantially before the extreme tail "
                    + "(p50="
                    + format(p50)
                    + " ms, p95="
                    + format(p95)
                    + " ms, p99="
                    + format(p99)
                    + " ms; p99 is "
                    + format(p99AboveMedianPercent)
                    + "% above p50). Investigate sustained queueing, "
                    + "thread scheduling, connection contention, or workload "
                    + "pressure, then compare the same percentile pattern "
                    + "across architectures and repeated runs.";
        }

        return "The median and p95 latencies are effectively aligned while "
                + "p99 is higher (p50="
                + format(p50)
                + " ms, p95="
                + format(p95)
                + " ms, p99="
                + format(p99)
                + " ms). Focus on the small set of slowest requests and "
                + "investigate intermittent blocking or scheduling effects, "
                + "then verify whether the p99 elevation repeats under the "
                + "same workload.";
    }

    private void analyzeArchitectureComparison(
            ExperimentResult experimentResult,
            List<BottleneckFinding> findings
    ) {

        if (experimentResult.getRunCount() < 2) {
            return;
        }

        ComparisonSummary summary =
                new com.serverbench.engine.benchmark.ExperimentAnalyzer()
                        .analyze(experimentResult);

        List<ArchitectureComparison> comparisons =
                summary.getComparisons();

        List<ArchitectureComparison> successfulComparisons =
                comparisons.stream()
                        .filter(
                                ArchitectureComparison::hasSuccessfulRuns
                        )
                        .toList();

        if (successfulComparisons.size() < 2) {
            return;
        }

        ArchitectureComparison highestThroughput =
                successfulComparisons.stream()
                        .max(
                                Comparator.comparingDouble(
                                        ArchitectureComparison::
                                                getAverageThroughput
                                )
                        )
                        .orElse(null);

        ArchitectureComparison lowestP99 =
                successfulComparisons.stream()
                        .min(
                                Comparator.comparingDouble(
                                        ArchitectureComparison::
                                                getAverageP99Latency
                                )
                        )
                        .orElse(null);

        if (highestThroughput != null
                && lowestP99 != null
                && highestThroughput.getArchitecture()
                        != lowestP99.getArchitecture()) {

            findings.add(
                    createFinding(
                            BottleneckCategory.ARCHITECTURE_COMPARISON,
                            BottleneckSeverity.INFO,
                            null,
                            "Throughput and tail-latency leaders differ",
                            "The architecture with the highest average throughput "
                                    + "is not the same architecture with the lowest "
                                    + "average p99 latency.",
                            "highestThroughputArchitecture="
                                    + highestThroughput
                                            .getArchitecture()
                                    + ", highestAverageThroughput="
                                    + format(
                                            highestThroughput
                                                    .getAverageThroughput()
                                    )
                                    + ", lowestP99Architecture="
                                    + lowestP99.getArchitecture()
                                    + ", lowestAverageP99LatencyMs="
                                    + format(
                                            lowestP99
                                                    .getAverageP99Latency()
                                    ),
                            "Treat throughput and tail latency as separate "
                                    + "performance dimensions rather than selecting "
                                    + "an overall winner without an explicit policy."
                    )
            );
        }

        ArchitectureComparison mostVariable =
                successfulComparisons.stream()
                        .max(
                                Comparator.comparingDouble(
                                        ArchitectureComparison::
                                                getThroughputVariationPercent
                                )
                        )
                        .orElse(null);

        if (mostVariable != null
                && mostVariable.getThroughputVariationPercent() > 0.0) {

            findings.add(
                    createFinding(
                            BottleneckCategory.THROUGHPUT_VARIATION,
                            BottleneckSeverity.INFO,
                            mostVariable.getArchitecture(),
                            "Throughput variation observed",
                            "Repeated successful runs for this architecture "
                                    + "produced different throughput measurements.",
                            "architecture="
                                    + mostVariable.getArchitecture()
                                    + ", successfulRuns="
                                    + mostVariable.getSuccessfulRuns()
                                    + ", throughputVariationPercent="
                                    + format(
                                            mostVariable
                                                    .getThroughputVariationPercent()
                                    ),
                            "Use repeated runs with the same configuration and "
                                    + "investigate environmental or workload factors "
                                    + "before treating a single throughput value as "
                                    + "representative."
                    )
            );
        }
    }

    private BottleneckFinding createFinding(
            BottleneckCategory category,
            BottleneckSeverity severity,
            ServerArchitecture architecture,
            String title,
            String description,
            String evidence,
            String recommendation
    ) {

        return new BottleneckFinding(
                category,
                severity,
                architecture == null
                        ? ""
                        : architecture.name(),
                title,
                description,
                evidence,
                recommendation
        );
    }

    private String format(double value) {

        return String.format(
                Locale.ROOT,
                "%.4f",
                value
        );
    }
}