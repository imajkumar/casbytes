package com.casbytes.core.shared.exception;

import com.casbytes.core.shared.api.ApiErrorBody;
import com.casbytes.core.shared.api.ApiResponse;
import com.casbytes.core.shared.api.ErrorCodes;
import com.casbytes.core.shared.api.ErrorDetail;
import com.casbytes.core.shared.util.RequestContextUtil;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, "Validation failed", details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(v -> ErrorDetail.builder()
                        .field(v.getPropertyPath().toString())
                        .message(v.getMessage())
                        .rejectedValue(v.getInvalidValue() == null ? null : String.valueOf(v.getInvalidValue()))
                        .build())
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, "Validation failed", details, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation", ex);
        return build(
                HttpStatus.CONFLICT,
                ErrorCodes.CONFLICT,
                "Data conflict while processing the request",
                List.of(),
                request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid credentials", List.of(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        HttpStatusCode code = ex.getStatusCode();
        HttpStatus status = code instanceof HttpStatus hs ? hs : HttpStatus.valueOf(code.value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        String errorCode =
                status == HttpStatus.SERVICE_UNAVAILABLE ? ErrorCodes.SERVICE_UNAVAILABLE : ErrorCodes.INTERNAL_ERROR;
        return build(status, errorCode, message, List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "Access denied", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.INTERNAL_ERROR,
                "An unexpected error occurred",
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
            String message,
            List<ErrorDetail> details,
            WebRequest request) {
        String correlationId = RequestContextUtil.correlationIdOrNew();
        String path = request.getDescription(false).replace("uri=", "");
        ApiErrorBody error = ApiErrorBody.builder()
                .code(code)
                .message(message)
                .details(details.isEmpty() ? null : details)
                .traceId(correlationId)
                .path(path)
                .build();
        ApiResponse<Void> body = ApiResponse.failure(error, correlationId, path);
        return ResponseEntity.status(status).body(body);
    }
}
