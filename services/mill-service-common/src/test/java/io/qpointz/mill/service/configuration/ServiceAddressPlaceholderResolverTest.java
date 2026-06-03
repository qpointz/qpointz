package io.qpointz.mill.service.configuration;

import io.qpointz.mill.service.descriptors.ServiceAddressScheme;
import io.qpointz.mill.service.providers.ServiceAddressPlaceholders;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceAddressPlaceholderResolverTest {

    private final ServiceAddressPlaceholderResolver resolver = new ServiceAddressPlaceholderResolver();

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldResolveLiteralEndpoint() {
        var raw = new ServiceAddressProperties.ServiceEndpointDescriptor("grpc", "localhost", "9090");

        var resolved = resolver.resolve(raw);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().scheme()).isEqualTo(ServiceAddressScheme.GRPC);
        assertThat(resolved.get().host()).isEqualTo("localhost");
        assertThat(resolved.get().port()).isEqualTo(9090);
    }

    @Test
    void shouldResolveGrpcRequestMixFromServletRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("ingress.example.com");
        request.setServerPort(443);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var raw = new ServiceAddressProperties.ServiceEndpointDescriptor(
                "grpc",
                ServiceAddressPlaceholders.REQUEST_HOST,
                "9090"
        );

        var resolved = resolver.resolve(raw);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().scheme()).isEqualTo(ServiceAddressScheme.GRPC);
        assertThat(resolved.get().host()).isEqualTo("ingress.example.com");
        assertThat(resolved.get().port()).isEqualTo(9090);
    }

    @Test
    void shouldResolveFullHttpRequestPlaceholders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("client.host");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var raw = new ServiceAddressProperties.ServiceEndpointDescriptor(
                ServiceAddressPlaceholders.REQUEST_SCHEME,
                ServiceAddressPlaceholders.REQUEST_HOST,
                ServiceAddressPlaceholders.REQUEST_PORT
        );

        var resolved = resolver.resolve(raw);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().scheme()).isEqualTo(ServiceAddressScheme.HTTP);
        assertThat(resolved.get().host()).isEqualTo("client.host");
        assertThat(resolved.get().port()).isEqualTo(8080);
    }

    @Test
    void shouldResolveHttpsFromForwardedHeadersBehindProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("mld5-run-service-703863947321.us-east1.run.app");
        request.setServerPort(80);
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "mld5-run-service-703863947321.us-east1.run.app");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var raw = new ServiceAddressProperties.ServiceEndpointDescriptor(
                ServiceAddressPlaceholders.REQUEST_SCHEME,
                ServiceAddressPlaceholders.REQUEST_HOST,
                ServiceAddressPlaceholders.REQUEST_PORT
        );

        var resolved = resolver.resolve(raw);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().scheme()).isEqualTo(ServiceAddressScheme.HTTPS);
        assertThat(resolved.get().host()).isEqualTo("mld5-run-service-703863947321.us-east1.run.app");
        assertThat(resolved.get().port()).isEqualTo(443);
    }

    @Test
    void shouldReturnEmptyWhenRequestPlaceholdersUsedWithoutActiveRequest() {
        var raw = new ServiceAddressProperties.ServiceEndpointDescriptor(
                ServiceAddressPlaceholders.REQUEST_SCHEME,
                "localhost",
                "8080"
        );

        assertThat(resolver.resolve(raw)).isEmpty();
    }
}
