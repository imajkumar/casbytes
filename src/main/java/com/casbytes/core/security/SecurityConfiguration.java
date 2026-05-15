package com.casbytes.core.security;

import com.casbytes.core.configuration.properties.CasbytesProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilter;
    private final CasbytesProperties casbytesProperties;
    private final ObjectProvider<JwtDecoder> jwtDecoder;
    private final ObjectProvider<JwtAuthenticationConverter> jwtAuthenticationConverter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        validateSecurityProfile();

        JwtDecoder decoder = jwtDecoder.getIfAvailable();

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                                    "/actuator/health",
                                    "/actuator/health/**",
                                    "/actuator/info")
                            .permitAll();
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                            .permitAll();
                    auth.requestMatchers("/error").permitAll();
                    auth.requestMatchers("/api/v1/health", "/api/v1/health/**")
                            .permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                            .permitAll();
                    if (casbytesProperties.getSecurity().isPermitApiWithoutAuth()) {
                        auth.requestMatchers("/api/v1/**").permitAll();
                    } else {
                        auth.requestMatchers("/api/v1/**").authenticated();
                    }
                    auth.requestMatchers("/actuator/**").authenticated();
                    auth.anyRequest().denyAll();
                })
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        if (decoder != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                jwt.decoder(decoder);
                jwtAuthenticationConverter.ifAvailable(jwt::jwtAuthenticationConverter);
            }));
            http.exceptionHandling(ex -> ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()));
        } else {
            JwtAuthenticationFilter filter = jwtAuthenticationFilter.getIfAvailable();
            if (filter != null) {
                http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
            }
        }

        return http.build();
    }

    private void validateSecurityProfile() {
        boolean stageOrProd = environment.acceptsProfiles(Profiles.of("stage", "prod"));
        if (!stageOrProd) {
            return;
        }
        if (casbytesProperties.getSecurity().isPermitApiWithoutAuth()) {
            return;
        }
        if (jwtDecoder.getIfAvailable() == null) {
            throw new IllegalStateException(
                    "Profiles 'stage' or 'prod' require authenticated APIs: enable "
                            + "casbytes.security.oauth2 with issuer-uri/jwk-set-uri (JwtDecoder), "
                            + "set casbytes.jwt.secret for symmetric JWT (OAuth2 disabled), "
                            + "or explicitly set casbytes.security.permit-api-without-auth=true for non-production debugging.");
        }
    }
}
