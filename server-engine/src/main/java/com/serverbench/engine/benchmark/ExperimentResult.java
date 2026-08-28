package com.serverbench.engine.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExperimentResult {

    private final String experimentId;
    private final String experimentName;

    private final List<ExperimentRunResult> runResults;

    public ExperimentResult(
            String experimentId,
            String experimentName
    ) {

        if (experimentId == null
                || experimentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment ID cannot be empty."
            );
        }

        if (experimentName == null
                || experimentName.isBlank()) {

            throw new IllegalArgumentException(
                    "Experiment name cannot be empty."
            );
        }

        this.experimentId =
                experimentId;

        this.experimentName =
                experimentName;

        this.runResults =
                new ArrayList<>();
    }

    public void addRunResult(
            ExperimentRunResult runResult
    ) {

        if (runResult == null) {

            throw new IllegalArgumentException(
                    "Experiment run result cannot be null."
            );
        }

        runResults.add(runResult);
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<ExperimentRunResult> getRunResults() {

        return Collections.unmodifiableList(
                runResults
        );
    }

    public int getRunCount() {
        return runResults.size();
    }

    public int getSuccessfulRunCount() {

        int count = 0;

        for (ExperimentRunResult runResult :
                runResults) {

            if (runResult.isSuccessful()) {
                count++;
            }
        }

        return count;
    }

    public int getFailedRunCount() {

        int count = 0;

        for (ExperimentRunResult runResult :
                runResults) {

            if (!runResult.isSuccessful()) {
                count++;
            }
        }

        return count;
    }

    public List<ExperimentRunResult>
    getResultsForArchitecture(
            ServerArchitecture architecture
    ) {

        if (architecture == null) {

            throw new IllegalArgumentException(
                    "Architecture cannot be null."
            );
        }

        List<ExperimentRunResult> results =
                new ArrayList<>();

        for (ExperimentRunResult runResult :
                runResults) {

            if (runResult.getArchitecture()
                    == architecture) {

                results.add(runResult);
            }
        }

        return Collections.unmodifiableList(
                results
        );
    }
}