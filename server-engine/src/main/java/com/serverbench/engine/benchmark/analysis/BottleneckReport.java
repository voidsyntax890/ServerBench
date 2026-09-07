package com.serverbench.engine.benchmark.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BottleneckReport {

    private final String experimentId;
    private final String experimentName;

    private final List<BottleneckFinding> findings;

    public BottleneckReport(
            String experimentId,
            String experimentName,
            List<BottleneckFinding> findings
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be blank."
            );
        }

        if (experimentName == null
                || experimentName.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment name cannot be blank."
            );
        }

        if (findings == null) {
            throw new IllegalArgumentException(
                    "Findings cannot be null."
            );
        }

        for (BottleneckFinding finding : findings) {

            if (finding == null) {
                throw new IllegalArgumentException(
                        "Finding cannot be null."
                );
            }
        }

        this.experimentId =
                experimentId.trim();

        this.experimentName =
                experimentName.trim();

        this.findings =
                Collections.unmodifiableList(
                        new ArrayList<>(findings)
                );
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<BottleneckFinding> getFindings() {
        return findings;
    }

    public boolean hasFindings() {
        return !findings.isEmpty();
    }

    public int getFindingCount() {
        return findings.size();
    }

    public long getCriticalFindingCount() {

        return findings.stream()
                .filter(
                        finding ->
                                finding.getSeverity()
                                        == BottleneckSeverity.CRITICAL
                )
                .count();
    }

    public long getWarningFindingCount() {

        return findings.stream()
                .filter(
                        finding ->
                                finding.getSeverity()
                                        == BottleneckSeverity.WARNING
                )
                .count();
    }

    public long getInfoFindingCount() {

        return findings.stream()
                .filter(
                        finding ->
                                finding.getSeverity()
                                        == BottleneckSeverity.INFO
                )
                .count();
    }
}