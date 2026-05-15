package com.casbytes.core.platform.health;

import com.casbytes.core.platform.health.dto.HealthReportDto;
import com.casbytes.core.platform.health.dto.HealthStatusDto;
import com.casbytes.core.shared.api.ApiResponse;
import com.casbytes.core.shared.constant.ApiRoutes;
import com.casbytes.core.shared.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.API_V1 + "/health")
@RequiredArgsConstructor
public class HealthV1Controller {

    private final PlatformHealthService platformHealthService;

    @GetMapping
    public ResponseEntity<ApiResponse<HealthReportDto>> overall(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                platformHealthService.overall(),
                RequestContextUtil.correlationIdOrNew(),
                request.getRequestURI()));
    }

    @GetMapping("/db")
    public ResponseEntity<ApiResponse<HealthStatusDto>> database(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                platformHealthService.database(),
                RequestContextUtil.correlationIdOrNew(),
                request.getRequestURI()));
    }

    @GetMapping("/redis")
    public ResponseEntity<ApiResponse<HealthStatusDto>> redis(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                platformHealthService.redis(),
                RequestContextUtil.correlationIdOrNew(),
                request.getRequestURI()));
    }

    @GetMapping("/kafka")
    public ResponseEntity<ApiResponse<HealthStatusDto>> kafka(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                platformHealthService.kafka(),
                RequestContextUtil.correlationIdOrNew(),
                request.getRequestURI()));
    }
}
