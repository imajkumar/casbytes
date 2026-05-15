package com.casbytes.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "casbytes.jwt")
public class JwtProperties {

    /**
     * Symmetric signing key for HMAC JWTs (future Auth service integration may switch to JWKS).
     */
    private String secret = "";

    private String issuer = "casbytes-auth";

    private String audience = "casbytes-core";

    /**
     * Access token TTL used by documentation and future token validation policies.
     */
    private long accessTokenTtlSeconds = 900;
}
