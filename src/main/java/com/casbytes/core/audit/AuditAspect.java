package com.casbytes.core.audit;

import com.casbytes.core.shared.util.RequestContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AuditAspect {

  @Around("@annotation(auditable)")
  public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
    long start = System.nanoTime();
    try {
      Object result = joinPoint.proceed();
      if (log.isInfoEnabled()) {
        log.info(
            "audit.success resource={} action={} method={} correlationId={} durationMs={}",
            auditable.resource(),
            auditable.action(),
            joinPoint.getSignature().toShortString(),
            RequestContextUtil.correlationIdOrNew(),
            durationMillis(start));
      }
      return result;
    } catch (Exception ex) {
      if (log.isWarnEnabled()) {
        log.warn(
            "audit.failure resource={} action={} method={} correlationId={} durationMs={} message={}",
            auditable.resource(),
            auditable.action(),
            joinPoint.getSignature().toShortString(),
            RequestContextUtil.correlationIdOrNew(),
            durationMillis(start),
            ex.getMessage());
      }
      throw ex;
    }
  }

  private long durationMillis(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000L;
  }
}
