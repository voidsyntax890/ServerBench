package com.serverbench.engine.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ExperimentAnalyzer {

    public ComparisonSummary analyze(
            ExperimentResult experimentResult
    ) {

        if (experimentResult == null) {
            throw new IllegalArgumentException(
                    "Experiment result cannot be null."
            );
        }

        if (experimentResult
                .getRunResults()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Experiment contains no run results."
            );
        }

        Map<
                ServerArchitecture,
                List<BenchmarkResult>
                > successfulResults =
                new EnumMap<>(
                        ServerArchitecture.class
                );

        Map<
                ServerArchitecture,
                Integer
                > totalRuns =
                new EnumMap<>(
                        ServerArchitecture.class
                );

        Map<
                ServerArchitecture,
                Integer
                > failedRuns =
                new EnumMap<>(
                        ServerArchitecture.class
                );

        for (ExperimentRunResult runResult :
                experimentResult.getRunResults()) {

            ServerArchitecture architecture =
                    runResult.getArchitecture();

            totalRuns.merge(
                    architecture,
                    1,
                    Integer::sum
            );

            if (runResult.isSuccessful()) {

                successfulResults
                        .computeIfAbsent(
                                architecture,
                                key -> new ArrayList<>()
                        )
                        .add(
                                runResult
                                        .getBenchmarkResult()
                        );

            } else {

                failedRuns.merge(
                        architecture,
                        1,
                        Integer::sum
                );
            }
        }

        List<ArchitectureComparison> comparisons =
                new ArrayList<>();

        for (ServerArchitecture architecture :
                totalRuns.keySet()) {

            List<BenchmarkResult> results =
                    successfulResults.getOrDefault(
                            architecture,
                            Collections.emptyList()
                    );

            if (!results.isEmpty()) {

                comparisons.add(
                        new ArchitectureComparison(
                                architecture,
                                results,
                                totalRuns.get(architecture),
                                failedRuns.getOrDefault(
                                        architecture,
                                        0
                                )
                        )
                );

            } else {

                comparisons.add(
                        ArchitectureComparison.failedOnly(
                                architecture,
                                totalRuns.get(architecture)
                        )
                );
            }
        }

        ServerArchitecture highestThroughput =
                findHighestThroughput(
                        comparisons
                );

        ServerArchitecture lowestAverageLatency =
                findLowestAverageLatency(
                        comparisons
                );

        ServerArchitecture lowestP95Latency =
                findLowestP95Latency(
                        comparisons
                );

        ServerArchitecture lowestP99Latency =
                findLowestP99Latency(
                        comparisons
                );

        ServerArchitecture highestSuccessRate =
                findHighestSuccessRate(
                        comparisons
                );

        return new ComparisonSummary(
                experimentResult.getExperimentId(),
                experimentResult.getExperimentName(),
                comparisons,
                highestThroughput,
                lowestAverageLatency,
                lowestP95Latency,
                lowestP99Latency,
                highestSuccessRate
        );
    }

    private ServerArchitecture findHighestThroughput(
            List<ArchitectureComparison> comparisons
    ) {

        return comparisons.stream()
                .filter(
                        ArchitectureComparison::
                                hasSuccessfulRuns
                )
                .max(
                        Comparator.comparingDouble(
                                ArchitectureComparison::
                                        getAverageThroughput
                        )
                )
                .map(
                        ArchitectureComparison::
                                getArchitecture
                )
                .orElse(null);
    }

    private ServerArchitecture findLowestAverageLatency(
            List<ArchitectureComparison> comparisons
    ) {

        return comparisons.stream()
                .filter(
                        ArchitectureComparison::
                                hasSuccessfulRuns
                )
                .min(
                        Comparator.comparingDouble(
                                ArchitectureComparison::
                                        getAverageLatency
                        )
                )
                .map(
                        ArchitectureComparison::
                                getArchitecture
                )
                .orElse(null);
    }

    private ServerArchitecture findLowestP95Latency(
            List<ArchitectureComparison> comparisons
    ) {

        return comparisons.stream()
                .filter(
                        ArchitectureComparison::
                                hasSuccessfulRuns
                )
                .min(
                        Comparator.comparingDouble(
                                ArchitectureComparison::
                                        getAverageP95Latency
                        )
                )
                .map(
                        ArchitectureComparison::
                                getArchitecture
                )
                .orElse(null);
    }

    private ServerArchitecture findLowestP99Latency(
            List<ArchitectureComparison> comparisons
    ) {

        return comparisons.stream()
                .filter(
                        ArchitectureComparison::
                                hasSuccessfulRuns
                )
                .min(
                        Comparator.comparingDouble(
                                ArchitectureComparison::
                                        getAverageP99Latency
                        )
                )
                .map(
                        ArchitectureComparison::
                                getArchitecture
                )
                .orElse(null);
    }

    private ServerArchitecture findHighestSuccessRate(
            List<ArchitectureComparison> comparisons
    ) {

        return comparisons.stream()
                .filter(
                        ArchitectureComparison::
                                hasSuccessfulRuns
                )
                .max(
                        Comparator.comparingDouble(
                                ArchitectureComparison::
                                        getAverageSuccessRate
                        )
                )
                .map(
                        ArchitectureComparison::
                                getArchitecture
                )
                .orElse(null);
    }
}