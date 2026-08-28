package com.serverbench.engine.benchmark;

import java.util.Collections;
import java.util.List;

public class ComparisonSummary {

    private final String experimentId;
    private final String experimentName;

    private final List<ArchitectureComparison> comparisons;

    private final ServerArchitecture highestThroughputArchitecture;
    private final ServerArchitecture lowestAverageLatencyArchitecture;
    private final ServerArchitecture lowestP95LatencyArchitecture;
    private final ServerArchitecture lowestP99LatencyArchitecture;
    private final ServerArchitecture highestSuccessRateArchitecture;

    public ComparisonSummary(
            String experimentId,
            String experimentName,
            List<ArchitectureComparison> comparisons,
            ServerArchitecture highestThroughputArchitecture,
            ServerArchitecture lowestAverageLatencyArchitecture,
            ServerArchitecture lowestP95LatencyArchitecture,
            ServerArchitecture lowestP99LatencyArchitecture,
            ServerArchitecture highestSuccessRateArchitecture
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        if (experimentName == null
                || experimentName.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment name cannot be empty."
            );
        }

        if (comparisons == null) {

            throw new IllegalArgumentException(
                    "Comparisons cannot be null."
            );
        }

        this.experimentId = experimentId;
        this.experimentName = experimentName;

        this.comparisons =
                Collections.unmodifiableList(
                        comparisons
                );

        this.highestThroughputArchitecture =
                highestThroughputArchitecture;

        this.lowestAverageLatencyArchitecture =
                lowestAverageLatencyArchitecture;

        this.lowestP95LatencyArchitecture =
                lowestP95LatencyArchitecture;

        this.lowestP99LatencyArchitecture =
                lowestP99LatencyArchitecture;

        this.highestSuccessRateArchitecture =
                highestSuccessRateArchitecture;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<ArchitectureComparison>
    getComparisons() {
        return comparisons;
    }

    public ServerArchitecture
    getHighestThroughputArchitecture() {
        return highestThroughputArchitecture;
    }

    public ServerArchitecture
    getLowestAverageLatencyArchitecture() {
        return lowestAverageLatencyArchitecture;
    }

    public ServerArchitecture
    getLowestP95LatencyArchitecture() {
        return lowestP95LatencyArchitecture;
    }

    public ServerArchitecture
    getLowestP99LatencyArchitecture() {
        return lowestP99LatencyArchitecture;
    }

    public ServerArchitecture
    getHighestSuccessRateArchitecture() {
        return highestSuccessRateArchitecture;
    }

    public void printSummary() {

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "       EXPERIMENT COMPARISON"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Experiment ID: "
                        + experimentId
        );

        System.out.println(
                "Experiment Name: "
                        + experimentName
        );

        System.out.println();

        System.out.printf(
                "%-20s %-15s %-15s %-15s %-15s %-15s%n",
                "Architecture",
                "Avg RPS",
                "Avg Latency",
                "Avg P95",
                "Avg P99",
                "Success Rate"
        );

        System.out.println(
                "--------------------------------------------------------------------------------"
        );

        for (ArchitectureComparison comparison :
                comparisons) {

            System.out.printf(
                    "%-20s %-15.2f %-15.2f %-15.2f %-15.2f %-15.2f%%%n",
                    comparison
                            .getArchitecture(),
                    comparison
                            .getAverageThroughput(),
                    comparison
                            .getAverageLatency(),
                    comparison
                            .getAverageP95Latency(),
                    comparison
                            .getAverageP99Latency(),
                    comparison
                            .getAverageSuccessRate()
            );
        }

        System.out.println();

        System.out.println(
                "Best Throughput: "
                        + formatArchitecture(
                                highestThroughputArchitecture
                        )
        );

        System.out.println(
                "Lowest Average Latency: "
                        + formatArchitecture(
                                lowestAverageLatencyArchitecture
                        )
        );

        System.out.println(
                "Lowest p95 Latency: "
                        + formatArchitecture(
                                lowestP95LatencyArchitecture
                        )
        );

        System.out.println(
                "Lowest p99 Latency: "
                        + formatArchitecture(
                                lowestP99LatencyArchitecture
                        )
        );

        System.out.println(
                "Highest Success Rate: "
                        + formatArchitecture(
                                highestSuccessRateArchitecture
                        )
        );

        System.out.println();

        System.out.println(
                "Note: These are metric-by-metric leaders."
        );

        System.out.println(
                "ServerBench does not declare an overall winner"
                        + " without an explicit analysis policy."
        );

        System.out.println(
                "========================================"
        );
    }

    private String formatArchitecture(
            ServerArchitecture architecture
    ) {

        return architecture == null
                ? "N/A"
                : architecture.toString();
    }
}