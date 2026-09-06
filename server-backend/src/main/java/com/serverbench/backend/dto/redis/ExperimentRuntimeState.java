package com.serverbench.backend.dto.redis;

public class ExperimentRuntimeState {

    private String experimentId;
    private String status;
    private String errorMessage;
    private String serverType;

    private String currentArchitecture;
    private Integer currentRepetition;
    private Integer completedRuns;
    private Integer totalRuns;

    private Integer attemptedRequests;
    private Integer successfulRequests;
    private Integer failedRequests;

    private Double throughputRequestsPerSecond;
    private Double averageLatencyMs;
    private Long elapsedTimeMs;

    public ExperimentRuntimeState() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getCurrentArchitecture() {
        return currentArchitecture;
    }

    public void setCurrentArchitecture(String currentArchitecture) {
        this.currentArchitecture = currentArchitecture;
    }

    public Integer getCurrentRepetition() {
        return currentRepetition;
    }

    public void setCurrentRepetition(Integer currentRepetition) {
        this.currentRepetition = currentRepetition;
    }

    public Integer getCompletedRuns() {
        return completedRuns;
    }

    public void setCompletedRuns(Integer completedRuns) {
        this.completedRuns = completedRuns;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public Integer getAttemptedRequests() {
        return attemptedRequests;
    }

    public void setAttemptedRequests(Integer attemptedRequests) {
        this.attemptedRequests = attemptedRequests;
    }

    public Integer getSuccessfulRequests() {
        return successfulRequests;
    }

    public void setSuccessfulRequests(Integer successfulRequests) {
        this.successfulRequests = successfulRequests;
    }

    public Integer getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(Integer failedRequests) {
        this.failedRequests = failedRequests;
    }

    public Double getThroughputRequestsPerSecond() {
        return throughputRequestsPerSecond;
    }

    public void setThroughputRequestsPerSecond(
            Double throughputRequestsPerSecond) {
        this.throughputRequestsPerSecond
                = throughputRequestsPerSecond;
    }

    public Double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(Double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public Long getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    public void setElapsedTimeMs(Long elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }
}
