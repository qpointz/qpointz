package io.qpointz.mill.service.configuration;

import io.qpointz.mill.service.descriptors.ServiceAddressScheme;
import io.qpointz.mill.service.providers.ServiceAddressPlaceholders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultExternalHostProviderTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldExposeOnlyResolvableEntries() {
        ServiceAddressProperties properties = new ServiceAddressProperties();
        properties.setExternals(Map.of(
                "static-http",
                new ServiceAddressProperties.ServiceEndpointDescriptor("http", "localhost", "8080"),
                "needs-request",
                new ServiceAddressProperties.ServiceEndpointDescriptor(
                        ServiceAddressPlaceholders.REQUEST_SCHEME,
                        ServiceAddressPlaceholders.REQUEST_HOST,
                        ServiceAddressPlaceholders.REQUEST_PORT
                )
        ));

        DefaultExternalHostProvider provider = new DefaultExternalHostProvider(
                properties,
                new ServiceAddressPlaceholderResolver()
        );

        assertThat(provider.getExternals()).containsOnlyKeys("static-http");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("edge.local");
        request.setServerPort(8443);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(provider.getExternals()).containsKeys("static-http", "needs-request");
        assertThat(provider.getExternals().get("needs-request").scheme()).isEqualTo(ServiceAddressScheme.HTTPS);
        assertThat(provider.getExternals().get("needs-request").host()).isEqualTo("edge.local");
        assertThat(provider.getExternals().get("needs-request").port()).isEqualTo(8443);
    }
}
