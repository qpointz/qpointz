package io.qpointz.mill.service.configuration;

/**
 * Configured HTTP listen port from {@code server.port} at application context refresh time.
 *
 * <p>When {@code server.port} is unset, the value matches Spring Boot's conventional default {@code 8080}.
 * When {@code server.port=0}, this value is {@code 0} until the embedded server starts; use
 * {@code local.server.port} after the web server has initialized if you need the OS-assigned port.
 *
 * @param value resolved listen port
 */
public record MillHttpListenPort(int value) {
}
