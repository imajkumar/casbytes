package com.casbytes.core.platform.health;

import com.casbytes.core.platform.health.dto.HealthStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisHealthService {

    private final RedisConnectionFactory redisConnectionFactory;

    public HealthStatusDto check() {
        try (var connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            return HealthStatusDto.builder().status("UP").detail("PING -> " + pong).build();
        } catch (Exception ex) {
            return HealthStatusDto.builder().status("DOWN").detail(ex.getMessage()).build();
        }
    }
}
