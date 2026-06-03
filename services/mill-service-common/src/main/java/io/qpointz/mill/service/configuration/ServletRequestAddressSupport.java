package io.qpointz.mill.service.configuration;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

/**
 * Client-visible scheme, host, and port from a servlet request, honoring reverse-proxy
 * {@code X-Forwarded-*} headers (e.g. Cloud Run, ingress) before raw {@link HttpServletRequest} values.
 */
public final class ServletRequestAddressSupport {

    private ServletRequestAddressSupport() {
    }

    /**
     * @param request current servlet request
     * @return client scheme (e.g. {@code https} behind TLS-terminating proxy)
     */
    public static String scheme(HttpServletRequest request) {
        String forwarded = firstHeaderValue(request, "X-Forwarded-Proto");
        if (forwarded != null) {
            return forwarded.toLowerCase(Locale.ROOT);
        }
        return request.getScheme();
    }

    /**
     * @param request current servlet request
     * @return client hostname without port
     */
    public static String host(HttpServletRequest request) {
        String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
        if (forwardedHost != null) {
            return hostOnly(forwardedHost);
        }
        String serverName = request.getServerName();
        if (serverName != null && !serverName.isBlank()) {
            return serverName;
        }
        String hostHeader = firstHeaderValue(request, "Host");
        return hostHeader != null ? hostOnly(hostHeader) : "";
    }

    /**
     * @param request current servlet request
     * @return client TCP port
     */
    public static int port(HttpServletRequest request) {
        String forwardedPort = firstHeaderValue(request, "X-Forwarded-Port");
        if (forwardedPort != null) {
            return Integer.parseInt(forwardedPort.trim());
        }

        String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
        if (forwardedHost != null) {
            int fromHost = portFromHostHeader(forwardedHost);
            if (fromHost > 0) {
                return fromHost;
            }
        }

        String resolvedScheme = scheme(request);
        int serverPort = request.getServerPort();
        if ("https".equalsIgnoreCase(resolvedScheme) && serverPort == 80) {
            return 443;
        }
        if ("http".equalsIgnoreCase(resolvedScheme) && serverPort == 443) {
            return 80;
        }
        return serverPort;
    }

    private static String firstHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.split(",")[0].trim();
    }

    private static String hostOnly(String hostHeader) {
        String trimmed = hostHeader.strip();
        if (trimmed.startsWith("[")) {
            int end = trimmed.indexOf(']');
            if (end > 0) {
                return trimmed.substring(0, end + 1);
            }
        }
        int colon = trimmed.lastIndexOf(':');
        if (colon > 0 && trimmed.indexOf(':') == colon) {
            return trimmed.substring(0, colon);
        }
        return trimmed;
    }

    private static int portFromHostHeader(String hostHeader) {
        String trimmed = hostHeader.strip();
        if (trimmed.startsWith("[")) {
            int end = trimmed.indexOf(']');
            if (end > 0 && end + 1 < trimmed.length() && trimmed.charAt(end + 1) == ':') {
                return Integer.parseInt(trimmed.substring(end + 2));
            }
            return -1;
        }
        int colon = trimmed.lastIndexOf(':');
        if (colon > 0 && trimmed.indexOf(':') == colon) {
            return Integer.parseInt(trimmed.substring(colon + 1));
        }
        return -1;
    }
}
