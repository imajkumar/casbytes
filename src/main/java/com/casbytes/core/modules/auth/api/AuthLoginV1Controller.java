package com.casbytes.core.modules.auth.api;

import com.casbytes.core.modules.auth.AuthLoginService;
import com.casbytes.core.modules.auth.dto.LoginRequest;
import com.casbytes.core.modules.auth.dto.LoginResponse;
import com.casbytes.core.shared.api.ApiResponse;
import com.casbytes.core.shared.constant.ApiRoutes;
import com.casbytes.core.shared.i18n.LocalizedApiResponseFactory;
import com.casbytes.core.shared.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.API_V1 + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and tokens")
public class AuthLoginV1Controller {

  private final AuthLoginService authLoginService;
  private final LocalizedApiResponseFactory apiResponseFactory;

  @PostMapping("/login")
  @Operation(summary = "Obtain JWT (bootstrap admin when enabled)")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest body, HttpServletRequest request) {
    LoginResponse tokens = authLoginService.login(body);
    return ResponseEntity.ok(
        apiResponseFactory.ok(
            tokens,
            "api.auth.login_success",
            RequestContextUtil.correlationIdOrNew(),
            request.getRequestURI()));
  }
}
