package com.casbytes.core.modules.auth;

import com.casbytes.core.configuration.properties.AuthProperties;
import com.casbytes.core.configuration.properties.JwtProperties;
import com.casbytes.core.modules.auth.dto.LoginRequest;
import com.casbytes.core.modules.auth.dto.LoginResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthLoginService {

    private final AuthProperties authProperties;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JwtEncoder> jwtEncoder;

    public LoginResponse login(LoginRequest request) {
        if (!authProperties.isBootstrapAdminEnabled()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!StringUtils.hasText(authProperties.getAdminEmail())
                || !StringUtils.hasText(authProperties.getAdminPasswordEncoded())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String email = request.getEmail().trim();
        if (!email.equalsIgnoreCase(authProperties.getAdminEmail().trim())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!passwordEncoder.matches(request.getPassword(), authProperties.getAdminPasswordEncoded())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        JwtEncoder encoder = jwtEncoder.getIfAvailable();
        if (encoder == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "JWT signing is not configured (set casbytes.jwt.secret and disable external OAuth2 issuer for local JWT mode)");
        }

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.getAccessTokenTtlSeconds());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(email)
                .audience(List.of(jwtProperties.getAudience()))
                .claim("roles", List.of("ADMIN"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInSeconds(jwtProperties.getAccessTokenTtlSeconds())
                .build();
    }
}
