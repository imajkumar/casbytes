package com.casbytes.core.security;

import com.casbytes.core.configuration.properties.CasbytesProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

@Configuration
public class OAuth2JwtDecoderConfiguration {

  @Bean
  @ConditionalOnProperty(
      prefix = "casbytes.security.oauth2",
      name = "enabled",
      havingValue = "true")
  public JwtDecoder casbytesJwtDecoder(CasbytesProperties properties) {
    CasbytesProperties.Oauth2 oauth2 = properties.getSecurity().getOauth2();
    if (StringUtils.hasText(oauth2.getJwkSetUri())) {
      return buildJwkDecoder(oauth2);
    }
    if (StringUtils.hasText(oauth2.getIssuerUri())) {
      return buildIssuerDecoder(oauth2);
    }
    throw new IllegalStateException(
        "casbytes.security.oauth2.enabled=true requires casbytes.security.oauth2.issuer-uri "
            + "or casbytes.security.oauth2.jwk-set-uri");
  }

  private static JwtDecoder buildIssuerDecoder(CasbytesProperties.Oauth2 oauth2) {
    NimbusJwtDecoder decoder =
        (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(oauth2.getIssuerUri());
    if (oauth2.getAudiences() != null && !oauth2.getAudiences().isEmpty()) {
      OAuth2TokenValidator<Jwt> audience =
          new JwtClaimValidator<List<String>>(
              "aud", aud -> aud != null && aud.stream().anyMatch(oauth2.getAudiences()::contains));
      decoder.setJwtValidator(
          new DelegatingOAuth2TokenValidator<>(
              JwtValidators.createDefaultWithIssuer(oauth2.getIssuerUri()), audience));
    }
    return decoder;
  }

  private static JwtDecoder buildJwkDecoder(CasbytesProperties.Oauth2 oauth2) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(oauth2.getJwkSetUri()).build();
    if (oauth2.getAudiences() != null && !oauth2.getAudiences().isEmpty()) {
      OAuth2TokenValidator<Jwt> audience =
          new JwtClaimValidator<List<String>>(
              "aud", aud -> aud != null && aud.stream().anyMatch(oauth2.getAudiences()::contains));
      decoder.setJwtValidator(
          new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), audience));
    }
    return decoder;
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "casbytes.security.oauth2",
      name = "enabled",
      havingValue = "true")
  @ConditionalOnBean(JwtDecoder.class)
  public JwtAuthenticationConverter jwtAuthenticationConverter(
      CasbytesProperties casbytesProperties) {
    JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
    scopes.setAuthorityPrefix("SCOPE_");
    scopes.setAuthoritiesClaimName("scope");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        (Converter<Jwt, Collection<GrantedAuthority>>)
            jwt -> {
              Collection<GrantedAuthority> granted = new HashSet<>(scopes.convert(jwt));
              if (casbytesProperties.getSecurity().getOauth2().isMapRealmRoles()) {
                granted.addAll(extractRealmRoles(jwt));
              }
              return granted;
            });
    converter.setPrincipalClaimName("sub");
    return converter;
  }

  @SuppressWarnings("unchecked")
  private static List<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    Object realmAccess = jwt.getClaim("realm_access");
    if (!(realmAccess instanceof Map<?, ?> map)) {
      return List.of();
    }
    Object rolesObj = map.get("roles");
    if (!(rolesObj instanceof List<?> roles)) {
      return List.of();
    }
    List<GrantedAuthority> out = new ArrayList<>();
    for (Object r : roles) {
      if (r != null) {
        out.add(new SimpleGrantedAuthority("ROLE_" + r));
      }
    }
    return out;
  }
}
