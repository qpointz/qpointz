package io.qpointz.mill.service.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Puts the Cloud Run / GCP trace id into SLF4J MDC so structured logs include
 * {@code logging.googleapis.com/trace} and nest under the request log in Cloud Logging.
 *
 * <p>Requires {@code GOOGLE_CLOUD_PROJECT} (set automatically on Cloud Run or via deploy env).
 * No-op when the project id or {@code X-Cloud-Trace-Context} header is absent.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CloudTraceMdcFilter extends OncePerRequestFilter {

    /** HTTP header carrying trace id and span id ({@code TRACE_ID/SPAN_ID;o=...}). */
    static final String TRACE_CONTEXT_HEADER = "X-Cloud-Trace-Context";

    /** MDC / JSON field recognized by Google Cloud Logging for log correlation. */
    static final String MDC_TRACE_KEY = "logging.googleapis.com/trace";

    private final String projectId;

    /**
     * @param projectId GCP project id from {@code GOOGLE_CLOUD_PROJECT}; blank disables trace MDC
     */
    public CloudTraceMdcFilter(@Value("${GOOGLE_CLOUD_PROJECT:}") String projectId) {
        this.projectId = projectId == null ? "" : projectId.strip();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (projectId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String traceId = traceIdFromHeader(request.getHeader(TRACE_CONTEXT_HEADER));
        if (traceId == null) {
            filterChain.doFilter(request, response);
            return;
        }
        MDC.put(MDC_TRACE_KEY, "projects/" + projectId + "/traces/" + traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_KEY);
        }
    }

    /**
     * @param traceContext value of {@link #TRACE_CONTEXT_HEADER}
     * @return trace id segment, or {@code null} if missing
     */
    static String traceIdFromHeader(String traceContext) {
        if (traceContext == null || traceContext.isBlank()) {
            return null;
        }
        String traceId = traceContext.split("/")[0].strip();
        return traceId.isEmpty() ? null : traceId;
    }
}
