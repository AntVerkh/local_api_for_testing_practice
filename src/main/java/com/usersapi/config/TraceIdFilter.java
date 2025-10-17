package com.usersapi.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get or generate trace ID
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = generateTraceId();
        }

        // Set trace ID in response header
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        // Set trace ID in MDC for logging
        MDC.put(TRACE_ID_MDC_KEY, traceId);

        try {
            // Log request
            logRequest(httpRequest, traceId);

            chain.doFilter(request, response);

            // Log response
            logResponse(httpResponse, traceId);

        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    private String generateTraceId() {
        return "trace-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void logRequest(HttpServletRequest request, String traceId) {
        System.out.printf("[%s] %s %s %s%n",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                getClientInfo(request)
        );
    }

    private void logResponse(HttpServletResponse response, String traceId) {
        System.out.printf("[%s] Response: %d%n",
                traceId,
                response.getStatus()
        );
    }

    private String getClientInfo(HttpServletRequest request) {
        return String.format("(IP: %s, User-Agent: %s)",
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
    }
}