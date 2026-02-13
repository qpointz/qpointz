package io.qpointz.mill.services.grinder.filters;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(1)
@ConditionalOnService("grinder")
public class GrinderUIFilter implements Filter {

    private static final String DEFAULT_UI_VERSION = "v1";

    private final String uiVersion;

    public GrinderUIFilter(@Autowired @Value("${mill.ui.version:" + DEFAULT_UI_VERSION +  "}") String version) {
        this.uiVersion = version;
    }

    public static GrinderUIFilter withDefaultUIVersion() {
        return new GrinderUIFilter(DEFAULT_UI_VERSION);
    }

    private static final Predicate<String> ROOT_REQUEST_PATTERN;

    private static final Predicate<String> ANY_APP_PREDICATE;

    private static final Predicate<String> APP_STATIC_RESOURCE_PATTERN;

    static {
        //if / or /app/index.html requested need to be redirected to /app/ to keep BrowserRouter working
        ROOT_REQUEST_PATTERN = Pattern
                .compile("^/(app\\/index\\.html)?$")
                .asPredicate();

        //all non
        ANY_APP_PREDICATE = Pattern
                .compile("^\\/app")
                .asPredicate();

        APP_STATIC_RESOURCE_PATTERN = Pattern
                .compile("^\\/app\\/.*\\.\\w+")
                .asPredicate();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        val req = (HttpServletRequest)servletRequest;
        val res = (HttpServletResponse)servletResponse;

        val requestURI = req.getRequestURI();
        log.trace("Filtering:{}", req.getRequestURI());

        //if / <root> request or direct /app/index.html request
        if (requestURI == null || requestURI.isEmpty() || ROOT_REQUEST_PATTERN.test(requestURI)) {
            res.sendRedirect("/app/");
            log.trace("Matched root request {} redirect to SPA /app/", requestURI);
            return;
        }

        //if non APP request or APP static request let it go
        if (!ANY_APP_PREDICATE.test(requestURI) || APP_STATIC_RESOURCE_PATTERN.test(requestURI)) {
            log.trace("Non SPA request passing through: {}", requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (ANY_APP_PREDICATE.test(requestURI) && ! HttpMethod.GET.matches(req.getMethod())) {
            log.warn("Non-GET request to SPA path is not allowed: {} {}", req.getMethod(), requestURI);
            res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED); // 405
            res.setHeader("Allow", "GET");
            return;
        }

        val dispatchTo = "/app/" + this.uiVersion + "/index.html";
        log.debug("Dispatching SPA to {}", dispatchTo);

        //for any other forward to /app/v1/index
        val requestDispatcher = servletRequest
                .getRequestDispatcher(dispatchTo);

        requestDispatcher
                .forward(req, res);
    }

}
