package com.casbytes.core.configuration.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "casbytes")
public class CasbytesProperties {

  private Security security = new Security();
  private Health health = new Health();

  @Data
  public static class Security {
    /**
     * When true, API routes are permitted without authentication (local/dev convenience only).
     */
    private boolean permitApiWithoutAuth = false;

    private Oauth2 oauth2 = new Oauth2();
  }

  @Data
  public static class Oauth2 {
    /**
     * When true, the API is protected by Spring Security OAuth2 Resource Server (JWT via issuer/JWKS).
     * Requires {@link #issuerUri} or {@link #jwkSetUri}.
     */
    private boolean enabled = false;

    /**
     * OIDC issuer (OpenID Provider). Spring resolves JWKS from issuer metadata.
     */
    private String issuerUri = "";

    /**
     * Static JWKS URL (use when issuer discovery is not available).
     */
    private String jwkSetUri = "";

    /**
     * Optional access-token audience values (at least one must match when non-empty).
     */
    private List<String> audiences = new ArrayList<>();

    /**
     * When true, map Keycloak-style {@code realm_access.roles} into {@code ROLE_*} authorities.
     */
    private boolean mapRealmRoles = false;
  }

  @Data
  public static class Health {
    private Kafka kafka = new Kafka();

    @Data
    public static class Kafka {
      private boolean enabled = true;
    }
  }
}
