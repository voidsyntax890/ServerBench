package com.serverbench.agent.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.serverbench.distributed.contracts.BenchmarkResultEvent;
import com.serverbench.distributed.contracts.BenchmarkTopics;

import tools.jackson.databind.ObjectMapper;

@Service
public class BenchmarkResultPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BenchmarkResultPublisher.class
            );

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BenchmarkResultPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(
            BenchmarkResultEvent event
    ) {

        if (event == null) {

            throw new IllegalArgumentException(
                    "Benchmark result event cannot be null."
            );
        }

        try {

            String payload =
                    objectMapper.writeValueAsString(
                            event
                    );

            kafkaTemplate
                    .send(
                            BenchmarkTopics.RESULTS,
                            event.jobId(),
                            payload
                    )
                    .whenComplete(
                            (result, exception) -> {

                                if (exception != null) {

                                    log.error(
                                            "Failed to publish benchmark result for job {}: {}",
                                            event.jobId(),
                                            exception.getMessage()
                                    );

                                } else {

                                    log.info(
                                            "Benchmark result published successfully: jobId={}",
                                            event.jobId()
                                    );
                                }
                            }
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to serialize benchmark result for job "
                            + event.jobId(),
                    exception
            );
        }
    }
}