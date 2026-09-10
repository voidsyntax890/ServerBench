package com.serverbench.backend.service;

import java.util.concurrent.ExecutionException;

import com.serverbench.distributed.contracts.BenchmarkJob;
import com.serverbench.distributed.contracts.BenchmarkTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class DistributedJobPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DistributedJobPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(BenchmarkJob job) {

        if (job == null) {
            throw new IllegalArgumentException(
                    "Distributed benchmark job cannot be null."
            );
        }

        try {
            String payload = objectMapper.writeValueAsString(job);

            kafkaTemplate.send(
                    BenchmarkTopics.JOBS,
                    job.jobId(),
                    payload
            ).get();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while publishing distributed benchmark job "
                            + job.jobId(),
                    exception
            );

        } catch (ExecutionException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Failed to publish distributed benchmark job "
                            + job.jobId(),
                    exception
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to serialize distributed benchmark job "
                            + job.jobId(),
                    exception
            );
        }
    }
}
