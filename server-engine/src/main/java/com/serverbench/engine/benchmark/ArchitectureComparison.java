package com.serverbench.engine.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchitectureComparison {

    private final ServerArchitecture architecture;

    private final int totalRuns;
    private final int successfulRuns;
    private final int failedRuns;

    private final double averageThroughput;
    private final double averageLatency;
    private final double averageP95Latency;
    private final double averageP99Latency;
    private final double averageSuccessRate;
    private final double averageErrorRate;

    private final double throughputVariationPercent;

    private final List<Double> throughputValues;

    private ArchitectureComparison(
            ServerArchitecture architecture,
            int totalRuns,
            int successfulRuns,
            int failedRuns,
            double averageThroughput,
            double averageLatency,
            double averageP95Latency,
            double averageP99Latency,
            double averageSuccessRate,
            double averageErrorRate,
            double throughputVariationPercent,
            List<Double> throughputValues
    ) {

        this.architecture = architecture;
        this.totalRuns = totalRuns;
        this.successfulRuns = successfulRuns;
        this.failedRuns = failedRuns;
        this.averageThroughput = averageThroughput;
        this.averageLatency = averageLatency;
        this.averageP95Latency = averageP95Latency;
        this.averageP99Latency = averageP99Latency;
        this.averageSuccessRate = averageSuccessRate;
        this.averageErrorRate = averageErrorRate;
        this.throughputVariationPercent =
                throughputVariationPercent;

        this.throughputValues =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                throughputValues
                        )
                );
    }

    public ArchitectureComparison(
            ServerArchitecture architecture,
            List<BenchmarkResult> results,
            int totalRuns,
            int failedRuns
    ) {

        if (architecture == null) {
            throw new IllegalArgumentException(
                    "Architecture cannot be null."
            );
        }

        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one successful result is required."
            );
        }

        if (totalRuns <= 0) {
            throw new IllegalArgumentException(
                    "Total runs must be greater than 0."
            );
        }

        if (failedRuns < 0
                || failedRuns > totalRuns) {

            throw new IllegalArgumentException(
                    "Invalid failed run count."
            );
        }

        int successfulRuns =
                results.size();

        double throughputSum = 0;
        double latencySum = 0;
        double p95Sum = 0;
        double p99Sum = 0;
        double successRateSum = 0;
        double errorRateSum = 0;

        List<Double> throughputs =
                new ArrayList<>();

        for (BenchmarkResult result :
                results) {

            if (result == null) {
                continue;
            }

            throughputSum +=
                    result
                            .getThroughputRequestsPerSecond();

            latencySum +=
                    result
                            .getAverageLatencyMs();

            p95Sum +=
                    result
                            .getP95LatencyMs();

            p99Sum +=
                    result
                            .getP99LatencyMs();

            successRateSum +=
                    result
                            .getSuccessRate();

            errorRateSum +=
                    result
                            .getErrorRate();

            throughputs.add(
                    result
                            .getThroughputRequestsPerSecond()
            );
        }

        if (throughputs.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid benchmark results supplied."
            );
        }

        int resultCount =
                throughputs.size();

        this.architecture =
                architecture;

        this.totalRuns =
                totalRuns;

        this.successfulRuns =
                successfulRuns;

        this.failedRuns =
                failedRuns;

        this.averageThroughput =
                throughputSum / resultCount;

        this.averageLatency =
                latencySum / resultCount;

        this.averageP95Latency =
                p95Sum / resultCount;

        this.averageP99Latency =
                p99Sum / resultCount;

        this.averageSuccessRate =
                successRateSum / resultCount;

        this.averageErrorRate =
                errorRateSum / resultCount;

        this.throughputValues =
                Collections.unmodifiableList(
                        throughputs
                );

        this.throughputVariationPercent =
                calculateVariationPercent(
                        throughputs
                );
    }

    public static ArchitectureComparison failedOnly(
            ServerArchitecture architecture,
            int totalRuns
    ) {

        return new ArchitectureComparison(
                architecture,
                totalRuns,
                0,
                totalRuns,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                100.0,
                0.0,
                Collections.emptyList()
        );
    }

    private double calculateVariationPercent(
            List<Double> values
    ) {

        if (values.size() <= 1) {
            return 0.0;
        }

        double sum = 0;

        for (double value : values) {
            sum += value;
        }

        double mean =
                sum / values.size();

        if (mean == 0) {
            return 0.0;
        }

        double squaredDifferenceSum = 0;

        for (double value : values) {

            double difference =
                    value - mean;

            squaredDifferenceSum +=
                    difference * difference;
        }

        double variance =
                squaredDifferenceSum
                        / values.size();

        double standardDeviation =
                Math.sqrt(variance);

        return (
                standardDeviation / mean
        ) * 100.0;
    }

    public ServerArchitecture getArchitecture() {
        return architecture;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public int getSuccessfulRuns() {
        return successfulRuns;
    }

    public int getFailedRuns() {
        return failedRuns;
    }

    public boolean hasSuccessfulRuns() {
        return successfulRuns > 0;
    }

    public double getAverageThroughput() {
        return averageThroughput;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public double getAverageP95Latency() {
        return averageP95Latency;
    }

    public double getAverageP99Latency() {
        return averageP99Latency;
    }

    public double getAverageSuccessRate() {
        return averageSuccessRate;
    }

    public double getAverageErrorRate() {
        return averageErrorRate;
    }

    public double getThroughputVariationPercent() {
        return throughputVariationPercent;
    }

    public List<Double> getThroughputValues() {
        return throughputValues;
    }
}