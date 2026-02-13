package io.qpointz.mill.services.grinder.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.io.IOException;

import static org.mockito.Mockito.*;

class GrinderUIFilterTest {

    private GrinderUIFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = GrinderUIFilter.withDefaultUIVersion();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "",
            "/app/index.html",
    })
    void shouldRedirectRootToApp(String url) throws Exception {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);
        verify(response).sendRedirect("/app/");
        verifyNoInteractions(chain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/app/style.css",
            "/app/assets/logo.png",
            "/app/logo.svg",
    })
    void shouldPassThroughStaticAppFile(String url) throws Exception {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/app/",
            "/app/assist/chat/",
            "/app/assist/chat",
            "/app/assist/chat/2342342m4mlkölsakdalösdkö",
    })
    void shouldForwardToAppIndexForSpaRoute(String url) throws Exception {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn("GET");

        val dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/app/v1/index.html")).thenReturn(dispatcher);
        filter.doFilter(request, response, chain);
        verify(dispatcher).forward(request, response);
        verifyNoInteractions(chain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/app/",
            "/app/assist/chat/",
            "/app/assist/chat",
            "/app/assist/chat/2342342m4mlkölsakdalösdkö",
    })
    void shouldRejectNonGetToSpaPath(String url) throws Exception {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn("POST");
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(response).setHeader("Allow", "GET");
        verifyNoInteractions(chain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/nl2sql/chats",
            "/services/export/schema/fff/x.csv",
            "/.well-known/mill/services",
    })
    void shouldLetApiRequestPass(String url) throws Exception {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn("GET");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "v1",
            "v2",
    })
    void versionRouting(String version) throws ServletException, IOException {
        val filterWithVersions = new GrinderUIFilter(version);
        when(request.getRequestURI()).thenReturn("/app/foo-bar");
        when(request.getMethod()).thenReturn("GET");
        val dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/app/" + version + "/index.html")).thenReturn(dispatcher);

        filterWithVersions.doFilter(request, response, chain);
        verify(dispatcher).forward(request, response);
        verifyNoInteractions(chain);
    }

    @Test
    void defaultRoutesToV1() throws ServletException, IOException {
        val filterWithVersions = GrinderUIFilter.withDefaultUIVersion(); //default version filter
        when(request.getRequestURI()).thenReturn("/app/foo-bar");
        when(request.getMethod()).thenReturn("GET");
        val dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/app/v1/index.html")).thenReturn(dispatcher);

        filterWithVersions.doFilter(request, response, chain);
        verify(dispatcher).forward(request, response);
        verifyNoInteractions(chain);
    }


}