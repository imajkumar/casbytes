package com.casbytes.core.security;

import com.casbytes.core.configuration.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

/**
 * Symmetric JWT (HS256) for this service when external OAuth2 issuer/JWKS is disabled.
 * Enables resource-server JWT validation and {@link com.casbytes.core.modules.auth.AuthLoginService} minting.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
    prefix = "casbytes.security.oauth2",
    name = "enabled",
    havingValue = "false",
    matchIfMissing = true)
@Conditional(NonBlankJwtSecretCondition.class)
public class LocalJwtSecurityConfiguration {

  @Bean
  public JwtEncoder casbytesJwtEncoder(JwtProperties jwtProperties) {
    SecretKey key = hmacKey(jwtProperties);
    return NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  public JwtDecoder casbytesJwtDecoder(JwtProperties jwtProperties) {
    SecretKey key = hmacKey(jwtProperties);
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
    OAuth2TokenValidator<Jwt> issuer =
        JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer());
    OAuth2TokenValidator<Jwt> audience = audienceValidator(jwtProperties);
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuer, audience));
    return decoder;
  }

  @Bean
  public JwtAuthenticationConverter casbytesLocalJwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
    scopes.setAuthorityPrefix("SCOPE_");
    scopes.setAuthoritiesClaimName("scope");

    JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
    roles.setAuthorityPrefix("ROLE_");
    roles.setAuthoritiesClaimName("roles");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        (Converter<Jwt, Collection<GrantedAuthority>>)
            jwt -> {
              Collection<GrantedAuthority> granted = new HashSet<>(scopes.convert(jwt));
              granted.addAll(roles.convert(jwt));
              if (granted.isEmpty()) {
                granted.add(new SimpleGrantedAuthority("ROLE_USER"));
              }
              return granted;
            });
    converter.setPrincipalClaimName("sub");
    return converter;
  }

  private static OAuth2TokenValidator<Jwt> audienceValidator(JwtProperties jwtProperties) {
    String expected = jwtProperties.getAudience();
    if (!StringUtils.hasText(expected)) {
      return jwt -> OAuth2TokenValidatorResult.success();
    }
    return jwt -> {
      List<String> aud = jwt.getAudience();
      if (aud != null && aud.contains(expected)) {
        return OAuth2TokenValidatorResult.success();
      }
      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_token", "Invalid audience", null));
    };
  }

  private static SecretKey hmacKey(JwtProperties jwtProperties) {
    byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(bytes, "HmacSHA256");
  }
}
