package com.casbytes.core.platform.health.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthReportDto {

  String status;
  Map<String, HealthStatusDto> checks;
}
