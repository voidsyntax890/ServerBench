package com.serverbench.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PerformanceExplanationRequest {

    @NotNull(message = "Finding index is required.")
    @Min(value = 0, message = "Finding index cannot be negative.")
    private Integer findingIndex;

    public PerformanceExplanationRequest() {
    }

    public Integer getFindingIndex() {
        return findingIndex;
    }

    public void setFindingIndex(
            Integer findingIndex
    ) {
        this.findingIndex = findingIndex;
    }
}