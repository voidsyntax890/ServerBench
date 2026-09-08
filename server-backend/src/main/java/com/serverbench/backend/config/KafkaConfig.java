package com.serverbench.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.serverbench.distributed.contracts.BenchmarkTopics.JOBS;
import static com.serverbench.distributed.contracts.BenchmarkTopics.PROGRESS;
import static com.serverbench.distributed.contracts.BenchmarkTopics.RESULTS;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic benchmarkJobsTopic() {
        return TopicBuilder
                .name(JOBS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic benchmarkProgressTopic() {
        return TopicBuilder
                .name(PROGRESS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic benchmarkResultsTopic() {
        return TopicBuilder
                .name(RESULTS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}