package com.serverbench.backend.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.serverbench.backend.dto.redis.ExperimentRuntimeState;

@Repository
public class ExperimentRuntimeStateRepository {

    private static final String KEY_PREFIX =
            "serverbench:experiment:runtime:";
            

    private static final Duration STATE_TTL =
            Duration.ofHours(1);

    private final RedisTemplate<String, ExperimentRuntimeState> redisTemplate;

    public ExperimentRuntimeStateRepository(
            RedisTemplate<String, ExperimentRuntimeState> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public void save(
            String experimentId,
            ExperimentRuntimeState state) {

        redisTemplate.opsForValue().set(
                buildKey(experimentId),
                state,
                STATE_TTL
        );
    }

    public ExperimentRuntimeState find(
            String experimentId) {

        return redisTemplate.opsForValue().get(
                buildKey(experimentId)
        );
    }

    public void delete(
            String experimentId) {

        redisTemplate.delete(
                buildKey(experimentId)
        );
    }

    private String buildKey(
            String experimentId) {

        return KEY_PREFIX + experimentId;
    }
}