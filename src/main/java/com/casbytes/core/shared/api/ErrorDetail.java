package com.casbytes.core.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetail {

  String field;
  String message;
  String rejectedValue;
}
