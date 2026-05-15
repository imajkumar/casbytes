package com.casbytes.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "casbytes.auth")
public class AuthProperties {

  /**
   * When true, login may fall back to {@link #adminEmail} / {@link #adminPasswordEncoded} if no matching
   * seeded {@code users} row exists (delegating format, e.g. {@code {noop}...} or {@code {bcrypt}...}).
   */
  private boolean bootstrapAdminEnabled = false;

  private String adminEmail = "";

  /**
   * Password in Spring Security delegating format, e.g. {@code {noop}secret} for local dev only.
   */
  private String adminPasswordEncoded = "";
}
