package com.casbytes.core.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LoginResponse {

  @JsonProperty("access_token")
  String accessToken;

  @JsonProperty("token_type")
  String tokenType;

  @JsonProperty("expires_in")
  long expiresInSeconds;
}
