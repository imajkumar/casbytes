package com.casbytes.core.modules.reference.api;

import com.casbytes.core.modules.reference.dto.ReferenceItemCreateRequest;
import com.casbytes.core.modules.reference.dto.ReferenceItemResponse;
import com.casbytes.core.modules.reference.service.ReferenceItemService;
import com.casbytes.core.shared.api.ApiResponse;
import com.casbytes.core.shared.constant.ApiRoutes;
import com.casbytes.core.shared.i18n.LocalizedApiResponseFactory;
import com.casbytes.core.shared.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.API_V1 + "/reference/items")
@RequiredArgsConstructor
@Tag(
    name = "Reference Items",
    description = "Sample bounded-context endpoints (template for ERP modules)")
public class ReferenceItemV1Controller {

  private final ReferenceItemService referenceItemService;
  private final LocalizedApiResponseFactory apiResponseFactory;

  @GetMapping
  @Operation(summary = "List active reference items")
  public ResponseEntity<ApiResponse<List<ReferenceItemResponse>>> list(HttpServletRequest request) {
    return ResponseEntity.ok(
        apiResponseFactory.ok(
            referenceItemService.listActive(),
            RequestContextUtil.correlationIdOrNew(),
            request.getRequestURI()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get reference item by id")
  public ResponseEntity<ApiResponse<ReferenceItemResponse>> getById(
      @PathVariable UUID id, HttpServletRequest request) {
    return ResponseEntity.ok(
        apiResponseFactory.ok(
            referenceItemService.getById(id),
            RequestContextUtil.correlationIdOrNew(),
            request.getRequestURI()));
  }

  @PostMapping
  @Operation(summary = "Create reference item")
  public ResponseEntity<ApiResponse<ReferenceItemResponse>> create(
      @Valid @RequestBody ReferenceItemCreateRequest body, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            apiResponseFactory.created(
                referenceItemService.create(body),
                RequestContextUtil.correlationIdOrNew(),
                request.getRequestURI()));
  }
}
