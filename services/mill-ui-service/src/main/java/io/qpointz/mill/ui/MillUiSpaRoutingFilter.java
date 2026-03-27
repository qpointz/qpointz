package io.qpointz.mill.ui;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Routes browser requests for the SPA: redirects root and direct {@code index.html} hits, forwards
 * deep-links to {@link MillUiProperties#getSpaIndexPath()}, blocks non-GET methods on SPA paths, and
 * passes API and static file requests through.
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "mill.ui.enabled", havingValue = "true", matchIfMissing = true)
public class MillUiSpaRoutingFilter implements Filter {

    private final MillUiProperties props;

    private final String normalizedBase;

    private final String redirectToAppSlash;

    private final Predicate<String> staticResourcePredicate;

    private final Predicate<String> underAppPrefix;

    /**
     * @param props bound {@code mill.ui.*} settings
     */
    public MillUiSpaRoutingFilter(MillUiProperties props) {
        this.props = props;
        this.normalizedBase = normalizeBasePath(props.getAppBasePath());
        this.redirectToAppSlash = normalizedBase.endsWith("/") ? normalizedBase : normalizedBase + "/";
        this.staticResourcePredicate = Pattern
                .compile("^" + Pattern.quote(normalizedBase) + "/.*\\.\\w+")
                .asPredicate();
        this.underAppPrefix = uri -> uri.equals(normalizedBase) || uri.startsWith(normalizedBase + "/");
    }

    private static String normalizeBasePath(String raw) {
        if (raw == null || raw.isBlank()) {
            return "/app";
        }
        String b = raw.trim();
        if (!b.startsWith("/")) {
            b = "/" + b;
        }
        if (b.endsWith("/") && b.length() > 1) {
            b = b.substring(0, b.length() - 1);
        }
        return b;
    }

    /**
     * {@inheritDoc}
     *
     * @param servletRequest  incoming request
     * @param servletResponse outgoing response
     * @param filterChain     remainder of the filter chain
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        val req = (HttpServletRequest) servletRequest;
        val res = (HttpServletResponse) servletResponse;

        val requestURI = req.getRequestURI();
        log.trace("Mill UI SPA filter: {}", requestURI);

        if (requestURI == null || requestURI.isEmpty() || isRootOrConfiguredIndex(requestURI)) {
            res.sendRedirect(redirectToAppSlash);
            log.trace("Redirect {} -> {}", requestURI, redirectToAppSlash);
            return;
        }

        if (!underAppPrefix.test(requestURI) || staticResourcePredicate.test(requestURI)) {
            log.trace("Pass through non-SPA or static: {}", requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!HttpMethod.GET.matches(req.getMethod())) {
            log.warn("Non-GET request to SPA path is not allowed: {} {}", req.getMethod(), requestURI);
            res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            res.setHeader("Allow", "GET");
            return;
        }

        val spaIndex = props.getSpaIndexPath();
        log.debug("Forwarding SPA path {} to {}", requestURI, spaIndex);
        val requestDispatcher = servletRequest.getRequestDispatcher(spaIndex);
        requestDispatcher.forward(req, res);
    }

    private boolean isRootOrConfiguredIndex(String uri) {
        return "/".equals(uri) || props.getSpaIndexPath().equals(uri);
    }
}
