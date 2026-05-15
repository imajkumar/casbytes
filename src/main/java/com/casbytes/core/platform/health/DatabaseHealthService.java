package com.casbytes.core.platform.health;

import com.casbytes.core.platform.health.dto.HealthStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseHealthService {

  private final JdbcTemplate jdbcTemplate;

  public HealthStatusDto check() {
    try {
      Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      if (one != null && one == 1) {
        return HealthStatusDto.builder().status("UP").detail("SELECT 1 succeeded").build();
      }
      return HealthStatusDto.builder().status("DEGRADED").detail("Unexpected query result").build();
    } catch (Exception ex) {
      return HealthStatusDto.builder().status("DOWN").detail(ex.getMessage()).build();
    }
  }
}
