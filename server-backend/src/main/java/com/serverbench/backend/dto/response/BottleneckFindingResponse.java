package com.serverbench.backend.dto.response;

import com.serverbench.engine.benchmark.analysis.BottleneckFinding;

public class BottleneckFindingResponse {

    private final String category;
    private final String severity;
    private final String architecture;
    private final String title;
    private final String description;
    private final String evidence;
    private final String recommendation;

    public BottleneckFindingResponse(
            BottleneckFinding finding
    ) {

        if (finding == null) {
            throw new IllegalArgumentException(
                    "Bottleneck finding cannot be null."
            );
        }

        this.category =
                finding.getCategory().name();
        this.severity =
                finding.getSeverity().name();
        this.architecture =
                finding.getArchitecture().isBlank()
                        ? null
                        : finding.getArchitecture();
        this.title = finding.getTitle();
        this.description = finding.getDescription();
        this.evidence = finding.getEvidence();
        this.recommendation =
                finding.getRecommendation();
    }

    public String getCategory() {
        return category;
    }

    public String getSeverity() {
        return severity;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
