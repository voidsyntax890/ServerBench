package com.serverbench.backend.dto.response;


import java.util.List;

import com.serverbench.engine.benchmark.ComparisonSummary;

public class ComparisonResponse {

    private final String experimentId;
    private final String experimentName;

    private final List<ArchitectureComparisonResponse> comparisons;

    private final String highestThroughputArchitecture;
    private final String lowestAverageLatencyArchitecture;
    private final String lowestP95LatencyArchitecture;
    private final String lowestP99LatencyArchitecture;
    private final String highestSuccessRateArchitecture;

    public ComparisonResponse(
            ComparisonSummary summary
    ) {

        this.experimentId =
                summary.getExperimentId();

        this.experimentName =
                summary.getExperimentName();

        this.comparisons =
                summary.getComparisons()
                        .stream()
                        .map(
                                ArchitectureComparisonResponse::new
                        )
                        .toList();

        this.highestThroughputArchitecture =
                formatArchitecture(
                        summary
                                .getHighestThroughputArchitecture()
                );

        this.lowestAverageLatencyArchitecture =
                formatArchitecture(
                        summary
                                .getLowestAverageLatencyArchitecture()
                );

        this.lowestP95LatencyArchitecture =
                formatArchitecture(
                        summary
                                .getLowestP95LatencyArchitecture()
                );

        this.lowestP99LatencyArchitecture =
                formatArchitecture(
                        summary
                                .getLowestP99LatencyArchitecture()
                );

        this.highestSuccessRateArchitecture =
                formatArchitecture(
                        summary
                                .getHighestSuccessRateArchitecture()
                );
    }

    private String formatArchitecture(
            Object architecture
    ) {

        return architecture == null
                ? null
                : architecture.toString();
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<ArchitectureComparisonResponse>
    getComparisons() {
        return comparisons;
    }

    public String getHighestThroughputArchitecture() {
        return highestThroughputArchitecture;
    }

    public String getLowestAverageLatencyArchitecture() {
        return lowestAverageLatencyArchitecture;
    }

    public String getLowestP95LatencyArchitecture() {
        return lowestP95LatencyArchitecture;
    }

    public String getLowestP99LatencyArchitecture() {
        return lowestP99LatencyArchitecture;
    }

    public String getHighestSuccessRateArchitecture() {
        return highestSuccessRateArchitecture;
    }
}