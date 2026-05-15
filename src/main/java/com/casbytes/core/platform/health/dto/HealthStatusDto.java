package com.casbytes.core.platform.health.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthStatusDto {

  /**
   * UP, DOWN, DEGRADED, SKIPPED, UNKNOWN
   */
  String status;

  String detail;
}
