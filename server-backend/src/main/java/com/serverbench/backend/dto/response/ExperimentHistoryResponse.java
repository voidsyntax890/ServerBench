package com.serverbench.backend.dto.response;

import java.util.List;

public class ExperimentHistoryResponse {

    private final int totalExperiments;
    private final List<ExperimentResponse> experiments;

    public ExperimentHistoryResponse(
            List<ExperimentResponse> experiments
    ) {

        this.experiments =
                List.copyOf(experiments);

        this.totalExperiments =
                this.experiments.size();
    }

    public int getTotalExperiments() {
        return totalExperiments;
    }

    public List<ExperimentResponse> getExperiments() {
        return experiments;
    }
}