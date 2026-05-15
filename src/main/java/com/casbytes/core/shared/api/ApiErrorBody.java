package com.casbytes.core.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorBody {

    String code;
    String message;
    List<ErrorDetail> details;
    String traceId;
    String path;
}
