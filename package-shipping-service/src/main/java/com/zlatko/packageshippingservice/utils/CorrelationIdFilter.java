package com.zlatko.packageshippingservice.utils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class CorrelationIdFilter implements Filter {

    /**
     * Filter method that adds the X-Correlation-ID and Request-ID headers to the response.
     * It also adds these headers to the MDC (Mapped Diagnostic Context) for logging purposes.
     *
     * @param servletRequest The request object
     * @param servletResponse The response object
     * @param chain The filter chain
     * @throws IOException If an I/O error occurs
     * @throws ServletException If a servlet exception occurs
     */
    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Retrieve or create X-Correlation-ID, needed for tracing requests across services
        String correlationId = request.getHeader(GlobalConstants.X_CORRELATION_ID);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateUniqueId();
            log.debug("Generated new correlation ID: {}", correlationId);
        }

        // Generate Request-Id
        String requestId = generateUniqueId();

        // Add the headers to the response
        response.addHeader(GlobalConstants.X_CORRELATION_ID, correlationId);
        response.addHeader(GlobalConstants.REQUEST_ID, requestId);

        // Log these headers for debugging purposes
        logHeaders(correlationId, requestId);

        // Add them to the MDC (Mapped Diagnostic Context) for logging across the entire application
        MDC.put(GlobalConstants.X_CORRELATION_ID, correlationId);
        MDC.put(GlobalConstants.REQUEST_ID, requestId);

        try {
            // Continue processing the request
            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(GlobalConstants.X_CORRELATION_ID);
            MDC.remove(GlobalConstants.REQUEST_ID);
        }
    }

    /**
     * Generate a unique identifier for the request as a UUID string.
     * @return A unique identifier
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Log the correlation ID and request ID for debugging purposes.
     *
     * @param correlationId The correlation ID
     * @param requestId The request ID
     */
    private void logHeaders(String correlationId, String requestId) {
        // Log the headers for tracing purposes
        log.trace("{}: {}, {}: {}", GlobalConstants.X_CORRELATION_ID, correlationId, GlobalConstants.REQUEST_ID, requestId);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization required
    }

    @Override
    public void destroy() {
        // No cleanup required
    }
}
