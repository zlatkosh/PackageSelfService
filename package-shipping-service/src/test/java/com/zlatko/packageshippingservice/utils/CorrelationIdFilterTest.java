package com.zlatko.packageshippingservice.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CorrelationIdFilterTest {

    private CorrelationIdFilter correlationIdFilter;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter();
    }

    @Nested
    class WhenNoCorrelationIdProvided {

        @Test
        @SneakyThrows
        void givenNoCorrelationId_whenFilterIsApplied_thenNewCorrelationIdAndRequestIdAreGenerated() {
            try (MockedStatic<MDC> mockedMDC = mockStatic(MDC.class)) {
                // Given: A request without an X-Correlation-ID header
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                MockFilterChain filterChain = new MockFilterChain();

                // When: The filter is applied
                correlationIdFilter.doFilter(request, response, filterChain);

                // Then: A new X-Correlation-ID and Request-ID should be generated and added to the response headers
                String correlationId = response.getHeader(GlobalConstants.X_CORRELATION_ID);
                String requestId = response.getHeader(GlobalConstants.REQUEST_ID);

                assertThat(correlationId).isNotNull();
                assertThat(correlationId).matches(GlobalConstants.UUID_REGEX_PATTERN);
                assertThat(requestId).isNotNull();
                assertThat(requestId).matches(GlobalConstants.UUID_REGEX_PATTERN);

                // Verify that MDC.put was called with the correct values
                mockedMDC.verify(() -> MDC.put(GlobalConstants.X_CORRELATION_ID, correlationId));
                mockedMDC.verify(() -> MDC.put(GlobalConstants.REQUEST_ID, requestId));
            }
        }
    }

    @Nested
    class WhenCorrelationIdIsProvided {

        @Test
        @SneakyThrows
        void givenCorrelationId_whenFilterIsApplied_thenExistingCorrelationIdIsUsedAndRequestIdIsGenerated() {
            try (MockedStatic<MDC> mockedMDC = mockStatic(MDC.class)) {
                // Given: A request with an X-Correlation-ID header
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                MockFilterChain filterChain = new MockFilterChain();
                String existingCorrelationId = UUID.randomUUID().toString();
                request.addHeader(GlobalConstants.X_CORRELATION_ID, existingCorrelationId);

                // When: The filter is applied
                correlationIdFilter.doFilter(request, response, filterChain);

                // Then: The existing X-Correlation-ID should be used, and a new Request-ID should be generated
                assertThat(response.getHeader(GlobalConstants.X_CORRELATION_ID)).isEqualTo(existingCorrelationId);
                String requestId = response.getHeader(GlobalConstants.REQUEST_ID);
                assertThat(requestId).isNotNull();
                assertThat(requestId).matches(GlobalConstants.UUID_REGEX_PATTERN);

                // Verify that MDC.put was called with the correct values
                mockedMDC.verify(() -> MDC.put(GlobalConstants.X_CORRELATION_ID, existingCorrelationId));
                mockedMDC.verify(() -> MDC.put(GlobalConstants.REQUEST_ID, requestId));
            }
        }
    }

    @Nested
    class FinalBlockTest {

        @Test
        @SneakyThrows
        void givenAnyRequest_whenFilterIsApplied_thenMdcIsCleanedUpAfterRequest() {
            try (MockedStatic<MDC> mockedMDC = mockStatic(MDC.class)) {
                // Given: A request without an X-Correlation-ID header
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                MockFilterChain filterChain = new MockFilterChain();

                // When: The filter is applied
                correlationIdFilter.doFilter(request, response, filterChain);

                // Then: Ensure that MDC.remove was called in the finally block
                mockedMDC.verify(() -> MDC.remove(GlobalConstants.X_CORRELATION_ID));
                mockedMDC.verify(() -> MDC.remove(GlobalConstants.REQUEST_ID));
            }
        }
    }
}
