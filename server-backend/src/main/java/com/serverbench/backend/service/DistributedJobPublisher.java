package com.serverbench.backend.service;

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

        try {
            String payload = objectMapper.writeValueAsString(job);

            kafkaTemplate.send(
                    BenchmarkTopics.JOBS,
                    job.jobId(),
                    payload
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to publish distributed benchmark job " + job.jobId(),
                    e
            );
        }
    }
}