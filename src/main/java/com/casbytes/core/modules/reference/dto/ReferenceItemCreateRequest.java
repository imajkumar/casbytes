package com.casbytes.core.modules.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReferenceItemCreateRequest {

    @NotBlank
    @Size(max = 64)
    String code;

    @NotBlank
    @Size(max = 255)
    String name;
}
