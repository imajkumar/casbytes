package com.casbytes.core.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  @JsonProperty("status_code")
  int statusCode;

  String message;

  boolean success;
  T data;
  ApiErrorBody error;
  ApiMeta meta;

  public static <T> ApiResponse<T> ok(
      T data, int statusCode, String message, String correlationId, String path) {
    return ApiResponse.<T>builder()
        .statusCode(statusCode)
        .message(message)
        .success(true)
        .data(data)
        .meta(
            ApiMeta.builder()
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .path(path)
                .build())
        .build();
  }

  /**
   * Envelope with an explicit {@code success} flag (e.g. aggregate health when {@code data.status} is not {@code UP}).
   */
  public static <T> ApiResponse<T> of(
      T data, boolean success, String message, String correlationId, String path) {
    return ApiResponse.<T>builder()
        .statusCode(200)
        .message(message)
        .success(success)
        .data(data)
        .meta(
            ApiMeta.builder()
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .path(path)
                .build())
        .build();
  }

  public static <T> ApiResponse<T> failure(
      ApiErrorBody errorBody, int statusCode, String correlationId, String path) {
    return ApiResponse.<T>builder()
        .statusCode(statusCode)
        .message(errorBody.getMessage())
        .success(false)
        .error(errorBody)
        .meta(
            ApiMeta.builder()
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .path(path)
                .build())
        .build();
  }
}
