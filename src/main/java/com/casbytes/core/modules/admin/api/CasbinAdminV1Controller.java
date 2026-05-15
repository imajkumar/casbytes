package com.casbytes.core.modules.admin.api;

import com.casbytes.core.configuration.properties.CasbinProperties;
import com.casbytes.core.infrastructure.casbin.CasbinPolicyManagementService;
import com.casbytes.core.shared.api.ApiResponse;
import com.casbytes.core.shared.constant.ApiRoutes;
import com.casbytes.core.shared.i18n.LocalizedApiResponseFactory;
import com.casbytes.core.shared.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping(ApiRoutes.API_V1 + "/admin/casbin")
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "casbytes.casbin",
    name = "reload-endpoint-enabled",
    havingValue = "true")
public class CasbinAdminV1Controller {

  private final CasbinPolicyManagementService casbinPolicyManagementService;
  private final CasbinProperties casbinProperties;
  private final LocalizedApiResponseFactory apiResponseFactory;

  @PostMapping("/reload")
  @PreAuthorize("hasAuthority('SCOPE_casbin.admin')")
  public ResponseEntity<ApiResponse<Map<String, String>>> reload(HttpServletRequest request) {
    casbinPolicyManagementService.reloadPolicies();
    return ResponseEntity.ok(
        apiResponseFactory.ok(
            Map.of(
                "status",
                "RELOADED",
                "policyStore",
                casbinProperties.getPolicyStore().name().toLowerCase()),
            RequestContextUtil.correlationIdOrNew(),
            request.getRequestURI()));
  }
}
