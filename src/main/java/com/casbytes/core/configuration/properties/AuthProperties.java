package com.casbytes.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "casbytes.auth")
public class AuthProperties {

    /**
     * When true, {@code POST /api/v1/auth/login} validates against {@link #adminEmail} and
     * {@link #adminPasswordEncoded} (delegating format, e.g. {@code {noop}...} or {@code {bcrypt}...}).
     */
    private boolean bootstrapAdminEnabled = false;

    private String adminEmail = "";

    /**
     * Password in Spring Security delegating format, e.g. {@code {noop}secret} for local dev only.
     */
    private String adminPasswordEncoded = "";
}
