package io.qpointz.mill.service.configuration;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CloudTraceMdcFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPutTraceInMdc_whenProjectAndTraceHeaderPresent() throws Exception {
        var filter = new CloudTraceMdcFilter("my-gcp-project");
        var request = new MockHttpServletRequest();
        request.addHeader(CloudTraceMdcFilter.TRACE_CONTEXT_HEADER, "abc123def456/789;o=1");
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                assertThat(MDC.get(CloudTraceMdcFilter.MDC_TRACE_KEY))
                        .isEqualTo("projects/my-gcp-project/traces/abc123def456");

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(CloudTraceMdcFilter.MDC_TRACE_KEY)).isNull();
    }

    @Test
    void shouldSkipMdc_whenProjectIdBlank() throws Exception {
        var filter = new CloudTraceMdcFilter("");
        var request = new MockHttpServletRequest();
        request.addHeader(CloudTraceMdcFilter.TRACE_CONTEXT_HEADER, "trace/1;o=1");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(MDC.get(CloudTraceMdcFilter.MDC_TRACE_KEY)).isNull();
    }

    @Test
    void shouldExtractTraceIdFromHeader() {
        assertThat(CloudTraceMdcFilter.traceIdFromHeader("tid/span;o=0")).isEqualTo("tid");
        assertThat(CloudTraceMdcFilter.traceIdFromHeader(null)).isNull();
        assertThat(CloudTraceMdcFilter.traceIdFromHeader("  ")).isNull();
    }
}
