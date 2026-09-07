package com.serverbench.backend.dto.response;

import java.util.List;

import com.serverbench.engine.benchmark.analysis.BottleneckReport;

public class BottleneckAnalysisResponse {

    private final String experimentId;
    private final String experimentName;
    private final List<BottleneckFindingResponse> findings;

    public BottleneckAnalysisResponse(
            BottleneckReport report
    ) {

        if (report == null) {
            throw new IllegalArgumentException(
                    "Bottleneck report cannot be null."
            );
        }

        this.experimentId =
                report.getExperimentId();
        this.experimentName =
                report.getExperimentName();
        this.findings =
                List.copyOf(
                        report.getFindings()
                                .stream()
                                .map(
                                        BottleneckFindingResponse::new
                                )
                                .toList()
                );
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<BottleneckFindingResponse> getFindings() {
        return findings;
    }

    public boolean hasFindings() {
        return !findings.isEmpty();
    }

    public int getFindingCount() {
        return findings.size();
    }
}
