package com.casbytes.core.modules.reference.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReferenceItemResponse {

  UUID id;
  String code;
  String name;
  boolean active;
  Instant createdAt;
  Instant updatedAt;
}
