package com.ticketbooking.seat.infrastructure.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis templates and atomic Redis Lua locking scripts.
 */
@Configuration
public class RedisConfig {

    private static final String LOCK_SEATS_LUA = "for i, key in ipairs(KEYS) do\n"
            + "    if redis.call('EXISTS', key) == 1 then\n"
            + "        return {0, key}\n"
            + "    end\n"
            + "end\n"
            + "for i, key in ipairs(KEYS) do\n"
            + "    redis.call('SET', key, ARGV[1], 'EX', ARGV[2])\n"
            + "end\n"
            + "return {1, 'SUCCESS'}\n";

    private static final String RELEASE_SEATS_LUA = "for i, key in ipairs(KEYS) do\n"
            + "    if redis.call('GET', key) ~= ARGV[1] then\n"
            + "        return {0, key}\n"
            + "    end\n"
            + "end\n"
            + "for i, key in ipairs(KEYS) do\n"
            + "    redis.call('DEL', key)\n"
            + "end\n"
            + "return {1, 'SUCCESS'}\n";

    /**
     * Configures StringRedisTemplate for String key and value serialization.
     *
     * @param connectionFactory Redis connection factory.
     * @return Configured RedisTemplate.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Bean definition for atomic multi-seat lock Lua script.
     *
     * @return RedisScript returning List of results.
     */
    @Bean
    @SuppressWarnings("rawtypes")
    public RedisScript<List> lockSeatsScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(LOCK_SEATS_LUA);
        script.setResultType(List.class);
        return script;
    }

    /**
     * Bean definition for atomic multi-seat release Lua script.
     *
     * @return RedisScript returning List of results.
     */
    @Bean
    @SuppressWarnings("rawtypes")
    public RedisScript<List> releaseSeatsScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(RELEASE_SEATS_LUA);
        script.setResultType(List.class);
        return script;
    }
}
