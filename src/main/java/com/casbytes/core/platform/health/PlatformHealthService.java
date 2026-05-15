package com.casbytes.core.platform.health;

import com.casbytes.core.platform.health.dto.HealthReportDto;
import com.casbytes.core.platform.health.dto.HealthStatusDto;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformHealthService {

  private final DatabaseHealthService databaseHealthService;
  private final ObjectProvider<RedisHealthService> redisHealthService;
  private final KafkaHealthService kafkaHealthService;

  public HealthReportDto overall() {
    Map<String, HealthStatusDto> checks = new LinkedHashMap<>();
    checks.put("database", databaseHealthService.check());
    RedisHealthService redis = redisHealthService.getIfAvailable();
    checks.put(
        "redis", redis == null ? skipped("Redis auto-configuration not active") : redis.check());
    checks.put("kafka", kafkaHealthService.check());
    return HealthReportDto.builder().status(aggregate(checks)).checks(checks).build();
  }

  public HealthStatusDto database() {
    return databaseHealthService.check();
  }

  public HealthStatusDto redis() {
    RedisHealthService redis = redisHealthService.getIfAvailable();
    return redis == null ? skipped("Redis auto-configuration not active") : redis.check();
  }

  public HealthStatusDto kafka() {
    return kafkaHealthService.check();
  }

  private static HealthStatusDto skipped(String detail) {
    return HealthStatusDto.builder().status("SKIPPED").detail(detail).build();
  }

  private static String aggregate(Map<String, HealthStatusDto> checks) {
    boolean anyDown =
        checks.values().stream().anyMatch(s -> "DOWN".equalsIgnoreCase(s.getStatus()));
    if (anyDown) {
      return "DOWN";
    }
    boolean anyDegraded =
        checks.values().stream().anyMatch(s -> "DEGRADED".equalsIgnoreCase(s.getStatus()));
    if (anyDegraded) {
      return "DEGRADED";
    }
    boolean anyUp = checks.values().stream().anyMatch(s -> "UP".equalsIgnoreCase(s.getStatus()));
    return anyUp ? "UP" : "DEGRADED";
  }
}
