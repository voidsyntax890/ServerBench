package com.serverbench.backend.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.serverbench.backend.service.DistributedBenchmarkResultService;
import com.serverbench.distributed.contracts.BenchmarkResultEvent;
import com.serverbench.distributed.contracts.BenchmarkTopics;

import tools.jackson.databind.ObjectMapper;

@Component
public class BenchmarkResultListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BenchmarkResultListener.class
            );

    private final ObjectMapper objectMapper;

    private final DistributedBenchmarkResultService
            resultService;

    public BenchmarkResultListener(
            ObjectMapper objectMapper,
            DistributedBenchmarkResultService resultService
    ) {

        this.objectMapper =
                objectMapper;

        this.resultService =
                resultService;
    }

    @KafkaListener(
            topics = BenchmarkTopics.RESULTS,
            groupId = "serverbench-backend-results"
    )
    public void handleBenchmarkResult(
            String payload
    ) {

        if (payload == null
                || payload.isBlank()) {

            log.error(
                    "Received empty benchmark result payload."
            );

            return;
        }

        final BenchmarkResultEvent event;

        try {

            event =
                    objectMapper.readValue(
                            payload,
                            BenchmarkResultEvent.class
                    );

        } catch (Exception exception) {

            log.error(
                    "Failed to deserialize benchmark result event: {}",
                    exception.getMessage()
            );

            return;
        }

        if (event == null) {

            log.error(
                    "Benchmark result payload deserialized to null."
            );

            return;
        }

        try {

            log.info(
                    "Backend received distributed benchmark result: "
                            + "jobId={}, experimentId={}, status={}, "
                            + "architecture={}, agentId={}",
                    event.jobId(),
                    event.experimentId(),
                    event.status(),
                    event.architecture(),
                    event.agentId()
            );

            resultService.acceptResult(
                    event
            );

        } catch (Exception exception) {

            /*
             * Do not acknowledge a corrupt/unprocessable event as a
             * successful business operation.
             *
             * The current listener logs the error. Retry/DLT policy
             * will be hardened in the dedicated M13 failure-handling
             * step.
             */
            log.error(
                    "Failed to process distributed benchmark result: "
                            + "jobId={}, error={}",
                    event.jobId(),
                    exception.getMessage(),
                    exception
            );
        }
    }
}