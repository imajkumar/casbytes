package com.casbytes.core.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestContextUtil {

    public static final String CORRELATION_ID_ATTRIBUTE = "casbytes.correlationId";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private RequestContextUtil() {}

    public static Optional<HttpServletRequest> currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
            return Optional.of(servletRequestAttributes.getRequest());
        }
        return Optional.empty();
    }

    public static String correlationIdOrNew() {
        return currentRequest()
                .map(r -> (String) r.getAttribute(CORRELATION_ID_ATTRIBUTE))
                .orElse(UUID.randomUUID().toString());
    }

    public static String requestPath() {
        return currentRequest().map(HttpServletRequest::getRequestURI).orElse("");
    }
}
