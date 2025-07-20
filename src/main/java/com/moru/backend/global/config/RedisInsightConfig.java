package com.moru.backend.global.config;

import com.moru.backend.domain.insight.dto.GlobalInsight;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisInsightConfig {
    @Bean
    public RedisTemplate<String, GlobalInsight> globalInsightRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, GlobalInsight> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key는 String
        template.setKeySerializer(new StringRedisSerializer());

        // Value는 JSON 직렬화를 위해 Jackson 사용
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(GlobalInsight.class));

        template.afterPropertiesSet();
        return template;
    }
}
