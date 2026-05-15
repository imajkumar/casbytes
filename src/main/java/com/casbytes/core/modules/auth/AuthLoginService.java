package com.casbytes.core.modules.auth;

import com.casbytes.core.configuration.properties.AuthProperties;
import com.casbytes.core.configuration.properties.JwtProperties;
import com.casbytes.core.modules.auth.domain.UserAccount;
import com.casbytes.core.modules.auth.domain.UserAccountRepository;
import com.casbytes.core.modules.auth.dto.LoginRequest;
import com.casbytes.core.modules.auth.dto.LoginResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

  /** JWT {@code roles} claim for YAML bootstrap login (same authority as seeded platform owner). */
  public static final String JWT_ROLE_PLATFORM_OWNER = "PLATFORM_OWNER";

  private final AuthProperties authProperties;
  private final JwtProperties jwtProperties;
  private final PasswordEncoder passwordEncoder;
  private final ObjectProvider<JwtEncoder> jwtEncoder;
  private final UserAccountRepository userAccountRepository;

  public LoginResponse login(LoginRequest request) {
    String email = request.getEmail().trim();
    if (!StringUtils.hasText(email) || request.getPassword() == null) {
      throw new BadCredentialsException("Invalid credentials");
    }

    Optional<UserAccount> account = authenticatedUser(email, request.getPassword());
    if (account.isPresent()) {
      return issueAccessToken(email, List.of(account.get().getRole().name()));
    }

    if (authProperties.isBootstrapAdminEnabled()
        && StringUtils.hasText(authProperties.getAdminEmail())
        && StringUtils.hasText(authProperties.getAdminPasswordEncoded())
        && email.equalsIgnoreCase(authProperties.getAdminEmail().trim())
        && passwordEncoder.matches(
            request.getPassword(), authProperties.getAdminPasswordEncoded())) {
      return issueAccessToken(email, List.of(JWT_ROLE_PLATFORM_OWNER));
    }

    throw new BadCredentialsException("Invalid credentials");
  }

  private Optional<UserAccount> authenticatedUser(String email, String rawPassword) {
    return userAccountRepository
        .findByEmailIgnoreCase(email)
        .filter(UserAccount::isEnabled)
        .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
  }

  private LoginResponse issueAccessToken(String subjectEmail, List<String> roles) {
    JwtEncoder encoder = jwtEncoder.getIfAvailable();
    if (encoder == null) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "JWT signing is not configured (set casbytes.jwt.secret and disable external OAuth2 issuer for local JWT mode)");
    }

    Instant now = Instant.now();
    Instant exp = now.plusSeconds(jwtProperties.getAccessTokenTtlSeconds());
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())
            .issuedAt(now)
            .expiresAt(exp)
            .subject(subjectEmail)
            .audience(List.of(jwtProperties.getAudience()))
            .claim("roles", roles)
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
