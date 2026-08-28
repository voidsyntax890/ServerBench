package com.serverbench.backend.dto.response;

public class ExperimentStartResponse {

    private final String experimentId;
    private final String status;
    private final String message;

    public ExperimentStartResponse(
            String experimentId,
            String status,
            String message
    ) {

        this.experimentId = experimentId;
        this.status = status;
        this.message = message;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}