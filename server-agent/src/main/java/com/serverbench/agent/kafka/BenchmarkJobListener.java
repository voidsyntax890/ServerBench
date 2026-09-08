package com.serverbench.agent.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.serverbench.agent.service.DistributedBenchmarkExecutor;
import com.serverbench.distributed.contracts.BenchmarkJob;
import com.serverbench.distributed.contracts.BenchmarkTopics;

import tools.jackson.databind.ObjectMapper;

@Component
public class BenchmarkJobListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BenchmarkJobListener.class
            );

    private final ObjectMapper objectMapper;
    private final DistributedBenchmarkExecutor executor;

    public BenchmarkJobListener(
            ObjectMapper objectMapper,
            DistributedBenchmarkExecutor executor
    ) {
        this.objectMapper = objectMapper;
        this.executor = executor;
    }

    @KafkaListener(
            topics = BenchmarkTopics.JOBS,
            groupId = "serverbench-agent"
    )
    public void handleBenchmarkJob(
            String payload
    ) {

        if (payload == null
                || payload.isBlank()) {

            log.error(
                    "Received empty benchmark job payload."
            );

            return;
        }

        final BenchmarkJob job;

        try {

            job =
                    objectMapper.readValue(
                            payload,
                            BenchmarkJob.class
                    );

        } catch (Exception exception) {

            log.error(
                    "Failed to deserialize benchmark job: {}",
                    exception.getMessage()
            );

            return;
        }

        if (job == null) {

            log.error(
                    "Kafka benchmark job payload deserialized to null."
            );

            return;
        }

        log.info(
                "ServerBench agent received benchmark job: jobId={}, architecture={}, target={}:{}",
                job.jobId(),
                job.architecture(),
                job.targetHost(),
                job.targetPort()
        );

        executor.execute(job);
    }
}