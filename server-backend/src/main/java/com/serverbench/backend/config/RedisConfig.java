package com.serverbench.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.serverbench.backend.dto.redis.ExperimentRuntimeState;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, ExperimentRuntimeState> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, ExperimentRuntimeState> template =
                new RedisTemplate<>();

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<ExperimentRuntimeState>
                valueSerializer =
                new JacksonJsonRedisSerializer<>(
                        ExperimentRuntimeState.class
                );

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }
}