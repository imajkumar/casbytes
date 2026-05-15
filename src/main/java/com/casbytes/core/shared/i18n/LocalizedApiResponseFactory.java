package com.casbytes.core.shared.i18n;

import com.casbytes.core.shared.api.ApiResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Builds {@link ApiResponse} bodies with {@code message} resolved from {@code messages*.properties}
 * using {@link LocaleContextHolder} (set from the {@code Accept-Language} header per request).
 */
@Component
@RequiredArgsConstructor
public class LocalizedApiResponseFactory {

  private final MessageSource messageSource;

  public <T> ApiResponse<T> ok(T data, String correlationId, String path) {
    return ok(data, HttpStatus.OK, "api.response.success", correlationId, path);
  }

  /** Same as {@link #ok(Object, String, String)} but with a request-specific {@code messageCode} from {@code messages*.properties}. */
  public <T> ApiResponse<T> ok(T data, String messageCode, String correlationId, String path) {
    return ok(data, HttpStatus.OK, messageCode, correlationId, path);
  }

  public <T> ApiResponse<T> created(T data, String correlationId, String path) {
    return ok(data, HttpStatus.CREATED, "api.response.created", correlationId, path);
  }

  public <T> ApiResponse<T> aggregateHealth(
      T data, boolean healthy, String correlationId, String path) {
    String code = healthy ? "api.response.success" : "api.health.unhealthy";
    return ApiResponse.of(data, healthy, text(code, null), correlationId, path);
  }

  private <T> ApiResponse<T> ok(
      T data, HttpStatus httpStatus, String messageCode, String correlationId, String path) {
    return ApiResponse.ok(data, httpStatus.value(), text(messageCode, null), correlationId, path);
  }

  public String text(String code, Object[] args) {
    Locale locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage(code, args, code, locale);
  }
}
