package com.casbytes.core.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * True when {@code casbytes.jwt.secret} is non-blank (used for symmetric JWT when OAuth2 RS is off).
 */
public final class NonBlankJwtSecretCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return StringUtils.hasText(context.getEnvironment().getProperty("casbytes.jwt.secret"));
  }
}
