package com.serverbench.backend.dto.response;

import java.util.List;

import com.serverbench.engine.benchmark.ArchitectureComparison;

public class ArchitectureComparisonResponse {

    private final String architecture;

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

    public ArchitectureComparisonResponse(
            ArchitectureComparison comparison
    ) {

        this.architecture =
                comparison
                        .getArchitecture()
                        .toString();

        this.totalRuns =
                comparison.getTotalRuns();

        this.successfulRuns =
                comparison.getSuccessfulRuns();

        this.failedRuns =
                comparison.getFailedRuns();

        this.averageThroughput =
                comparison.getAverageThroughput();

        this.averageLatency =
                comparison.getAverageLatency();

        this.averageP95Latency =
                comparison.getAverageP95Latency();

        this.averageP99Latency =
                comparison.getAverageP99Latency();

        this.averageSuccessRate =
                comparison.getAverageSuccessRate();

        this.averageErrorRate =
                comparison.getAverageErrorRate();

        this.throughputVariationPercent =
                comparison
                        .getThroughputVariationPercent();

        this.throughputValues =
                comparison.getThroughputValues();
    }

    public String getArchitecture() {
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