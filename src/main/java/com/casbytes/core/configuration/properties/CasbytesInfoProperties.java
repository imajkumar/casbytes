package com.casbytes.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "casbytes.info")
public class CasbytesInfoProperties {

  /** Product title shown on the HTML root page. */
  private String productName = "CasBytes Core ERP";

  /** Short description under the title. */
  private String tagline =
      "CasBytes enterprise core service — modular APIs, persistence, security, and integrations.";

  /** Used when Spring Boot build-info is not on the classpath (e.g. some IDE runs). */
  private String versionFallback = "0.1.0-SNAPSHOT";
}
