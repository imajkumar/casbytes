package com.casbytes.core.observability;

import com.casbytes.core.shared.util.RequestContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        request.setAttribute(RequestContextUtil.CORRELATION_ID_ATTRIBUTE, correlationId);
        response.setHeader(RequestContextUtil.CORRELATION_ID_HEADER, correlationId);
        response.setHeader(RequestContextUtil.TRACE_ID_HEADER, correlationId);

        MDC.put("correlationId", correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String header = request.getHeader(RequestContextUtil.CORRELATION_ID_HEADER);
        if (header == null || header.isBlank()) {
            header = request.getHeader(RequestContextUtil.TRACE_ID_HEADER);
        }
        if (header == null || header.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return header.trim();
    }
}
