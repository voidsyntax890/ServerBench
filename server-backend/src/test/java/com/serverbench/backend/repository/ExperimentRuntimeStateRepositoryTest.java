package com.serverbench.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.serverbench.backend.dto.redis.ExperimentRuntimeState;

@SpringBootTest
class ExperimentRuntimeStateRepositoryTest {

    @Autowired
    private ExperimentRuntimeStateRepository repository;

    @Test
    void shouldSaveAndReadRuntimeState() {

        String experimentId =
                "redis-test-experiment";

        ExperimentRuntimeState state =
                new ExperimentRuntimeState();

        state.setExperimentId(experimentId);
        state.setStatus("RUNNING");

        state.setCurrentArchitecture(
                "VIRTUAL_THREAD"
        );
        state.setCurrentRepetition(1);
        state.setCompletedRuns(2);
        state.setTotalRuns(4);

        state.setAttemptedRequests(100);
        state.setSuccessfulRequests(95);
        state.setFailedRequests(5);

        state.setThroughputRequestsPerSecond(
                50.5
        );
        state.setAverageLatencyMs(
                12.25
        );
        state.setElapsedTimeMs(
                2000L
        );

        repository.save(
                experimentId,
                state
        );

        ExperimentRuntimeState restored =
                repository.find(
                        experimentId
                );

        assertNotNull(restored);

        assertEquals(
                experimentId,
                restored.getExperimentId()
        );

        assertEquals(
                "RUNNING",
                restored.getStatus()
        );

        assertEquals(
                "VIRTUAL_THREAD",
                restored.getCurrentArchitecture()
        );

        assertEquals(
                2,
                restored.getCompletedRuns()
        );

        assertEquals(
                95,
                restored.getSuccessfulRequests()
        );

        assertEquals(
                50.5,
                restored.getThroughputRequestsPerSecond()
        );

        repository.delete(
                experimentId
        );

        assertEquals(
                null,
                repository.find(experimentId)
        );
    }
}