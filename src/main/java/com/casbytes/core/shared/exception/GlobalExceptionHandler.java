package com.casbytes.core.shared.exception;

import com.casbytes.core.shared.api.ApiErrorBody;
import com.casbytes.core.shared.api.ApiResponse;
import com.casbytes.core.shared.api.ErrorCodes;
import com.casbytes.core.shared.api.ErrorDetail;
import com.casbytes.core.shared.util.RequestContextUtil;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    List<ErrorDetail> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::toDetail)
            .collect(Collectors.toList());
    return build(
        HttpStatus.BAD_REQUEST,
        ErrorCodes.VALIDATION_FAILED,
        "api.error.validation_failed",
        null,
        details,
        request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    List<ErrorDetail> details =
        ex.getConstraintViolations().stream()
            .map(
                v ->
                    ErrorDetail.builder()
                        .field(v.getPropertyPath().toString())
                        .message(v.getMessage())
                        .rejectedValue(
                            v.getInvalidValue() == null
                                ? null
                                : String.valueOf(v.getInvalidValue()))
                        .build())
            .collect(Collectors.toList());
    return build(
        HttpStatus.BAD_REQUEST,
        ErrorCodes.VALIDATION_FAILED,
        "api.error.validation_failed",
        null,
        details,
        request);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(
      ResourceNotFoundException ex, WebRequest request) {
    if (ex.hasTypedResource()) {
      return build(
          HttpStatus.NOT_FOUND,
          ex.getCode(),
          "api.error.resource_not_found",
          new Object[] {ex.getResourceType(), ex.getResourceId()},
          List.of(),
          request);
    }
    return build(
        HttpStatus.NOT_FOUND,
        ex.getCode(),
        "api.error.not_found_detail",
        new Object[] {ex.getMessage()},
        List.of(),
        request);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(
      BusinessException ex, WebRequest request) {
    return build(
        HttpStatus.CONFLICT,
        ex.getCode(),
        "api.error.business",
        new Object[] {ex.getMessage()},
        List.of(),
        request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
      DataIntegrityViolationException ex, WebRequest request) {
    log.warn("Data integrity violation", ex);
    return build(
        HttpStatus.CONFLICT,
        ErrorCodes.CONFLICT,
        "api.error.conflict_data",
        null,
        List.of(),
        request);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
      BadCredentialsException ex, WebRequest request) {
    return build(
        HttpStatus.UNAUTHORIZED,
        ErrorCodes.UNAUTHORIZED,
        "api.error.invalid_credentials",
        null,
        List.of(),
        request);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiResponse<Void>> handleResponseStatus(
      ResponseStatusException ex, WebRequest request) {
    HttpStatusCode code = ex.getStatusCode();
    HttpStatus status = code instanceof HttpStatus hs ? hs : HttpStatus.valueOf(code.value());
    String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
    String errorCode =
        status == HttpStatus.SERVICE_UNAVAILABLE
            ? ErrorCodes.SERVICE_UNAVAILABLE
            : ErrorCodes.INTERNAL_ERROR;
    String messageKey =
        status == HttpStatus.SERVICE_UNAVAILABLE
            ? "api.error.service_unavailable"
            : "api.error.internal";
    return build(status, errorCode, messageKey, null, List.of(), request, reason);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
      AccessDeniedException ex, WebRequest request) {
    return build(
        HttpStatus.FORBIDDEN,
        ErrorCodes.FORBIDDEN,
        "api.error.access_denied",
        null,
        List.of(),
        request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, WebRequest request) {
    log.error("Unhandled exception", ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCodes.INTERNAL_ERROR,
        "api.error.internal",
        null,
        List.of(),
        request);
  }

  private ErrorDetail toDetail(FieldError fe) {
    return ErrorDetail.builder()
        .field(fe.getField())
        .message(fe.getDefaultMessage())
        .rejectedValue(fe.getRejectedValue() == null ? null : String.valueOf(fe.getRejectedValue()))
        .build();
  }

  private ResponseEntity<ApiResponse<Void>> build(
      HttpStatus status,
      String code,
      String messageKey,
      Object[] messageArgs,
      List<ErrorDetail> details,
      WebRequest request) {
    return build(status, code, messageKey, messageArgs, details, request, null);
  }

  private ResponseEntity<ApiResponse<Void>> build(
      HttpStatus status,
      String code,
      String messageKey,
      Object[] messageArgs,
      List<ErrorDetail> details,
      WebRequest request,
      String defaultMessageOverride) {
    String correlationId = RequestContextUtil.correlationIdOrNew();
    String path = request.getDescription(false).replace("uri=", "");
    Locale locale = LocaleContextHolder.getLocale();
    String defaultMessage = defaultMessageOverride != null ? defaultMessageOverride : messageKey;
    String localizedMessage =
        messageSource.getMessage(messageKey, messageArgs, defaultMessage, locale);
    ApiErrorBody error =
        ApiErrorBody.builder()
            .code(code)
            .message(localizedMessage)
            .details(details.isEmpty() ? null : details)
            .traceId(correlationId)
            .path(path)
            .build();
    ApiResponse<Void> body = ApiResponse.failure(error, status.value(), correlationId, path);
    return ResponseEntity.status(status).body(body);
  }
}
