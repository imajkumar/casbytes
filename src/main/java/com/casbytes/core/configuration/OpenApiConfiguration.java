package com.casbytes.core.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  public static final String BEARER_SCHEME = "bearer-jwt";

  @Bean
  public OpenAPI casbytesOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("CasBytes Core Service API")
                .description(
                    "Core ERP platform HTTP API (version 1). Send **Accept-Language** (`en` or `fr`) "
                        + "to localize top-level `message` (and error `message`) strings in JSON envelopes.")
                .version("v1")
                .contact(new Contact().name("CasBytes").url("https://casbytes.com")))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Bearer JWT: symmetric tokens from POST /api/v1/auth/login when "
                                + "OAuth2 is disabled and casbytes.jwt.secret is set, or tokens from your OIDC provider when OAuth2 is enabled.")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }
}
