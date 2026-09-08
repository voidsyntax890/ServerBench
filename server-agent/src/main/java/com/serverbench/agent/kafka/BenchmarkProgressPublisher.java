package com.serverbench.agent.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.serverbench.distributed.contracts.BenchmarkProgressEvent;
import com.serverbench.distributed.contracts.BenchmarkTopics;

import tools.jackson.databind.ObjectMapper;

@Service
public class BenchmarkProgressPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BenchmarkProgressPublisher.class
            );

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BenchmarkProgressPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(
            BenchmarkProgressEvent event
    ) {

        if (event == null) {

            log.warn(
                    "Ignoring null benchmark progress event."
            );

            return;
        }

        try {

            String payload =
                    objectMapper.writeValueAsString(
                            event
                    );

            kafkaTemplate
                    .send(
                            BenchmarkTopics.PROGRESS,
                            event.jobId(),
                            payload
                    )
                    .whenComplete(
                            (result, exception) -> {

                                if (exception != null) {

                                    log.warn(
                                            "Failed to publish progress for job {}: {}",
                                            event.jobId(),
                                            exception.getMessage()
                                    );
                                }
                            }
                    );

        } catch (Exception exception) {

            log.warn(
                    "Failed to serialize benchmark progress for job {}: {}",
                    event.jobId(),
                    exception.getMessage()
            );
        }
    }
}