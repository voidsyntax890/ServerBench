package com.serverbench.backend.dto.response;

public class PerformanceExplanationResponse {

    private final String experimentId;
    private final int findingIndex;
    private final String findingTitle;
    private final String explanation;

    public PerformanceExplanationResponse(
            String experimentId,
            int findingIndex,
            String findingTitle,
            String explanation
    ) {

        this.experimentId =
                experimentId;

        this.findingIndex =
                findingIndex;

        this.findingTitle =
                findingTitle;

        this.explanation =
                explanation;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public int getFindingIndex() {
        return findingIndex;
    }

    public String getFindingTitle() {
        return findingTitle;
    }

    public String getExplanation() {
        return explanation;
    }
}