package com.casbytes.core.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    boolean success;
    T data;
    ApiErrorBody error;
    ApiMeta meta;

    public static <T> ApiResponse<T> ok(T data, String correlationId, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .meta(ApiMeta.builder()
                        .timestamp(Instant.now())
                        .correlationId(correlationId)
                        .path(path)
                        .build())
                .build();
    }

    public static ApiResponse<Void> okEmpty(String correlationId, String path) {
        return ok(null, correlationId, path);
    }

    public static <T> ApiResponse<T> failure(
            ApiErrorBody errorBody, String correlationId, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(errorBody)
                .meta(ApiMeta.builder()
                        .timestamp(Instant.now())
                        .correlationId(correlationId)
                        .path(path)
                        .build())
                .build();
    }
}
