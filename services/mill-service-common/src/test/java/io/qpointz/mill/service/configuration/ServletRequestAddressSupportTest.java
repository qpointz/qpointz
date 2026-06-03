package io.qpointz.mill.service.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ServletRequestAddressSupportTest {

    @Test
    void shouldPreferForwardedProtoAndDefaultHttpsPort() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerPort(80);
        request.addHeader("X-Forwarded-Proto", "https");

        assertThat(ServletRequestAddressSupport.scheme(request)).isEqualTo("https");
        assertThat(ServletRequestAddressSupport.port(request)).isEqualTo(443);
    }

    @Test
    void shouldParseHostAndPortFromForwardedHostHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Host", "edge.example.com:8443");
        request.addHeader("X-Forwarded-Proto", "https");

        assertThat(ServletRequestAddressSupport.host(request)).isEqualTo("edge.example.com");
        assertThat(ServletRequestAddressSupport.port(request)).isEqualTo(8443);
    }
}
