package com.casbytes.core.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Public user snapshot returned with a successful login (no secrets). */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginUserDto {

  UUID id;

  String email;

  String role;

  @JsonProperty("first_name")
  String firstName;

  @JsonProperty("last_name")
  String lastName;

  @JsonProperty("display_name")
  String displayName;

  String gender;

  String phone;

  String mobile;

  @JsonProperty("job_title")
  String jobTitle;

  String department;

  String locale;

  @JsonProperty("time_zone")
  String timeZone;
}
