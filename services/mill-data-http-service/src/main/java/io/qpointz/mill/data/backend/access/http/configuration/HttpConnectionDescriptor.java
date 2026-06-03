package io.qpointz.mill.data.backend.access.http.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.descriptors.ServiceAddressScheme;
import io.qpointz.mill.service.providers.ExternalHostLookup;
import io.qpointz.mill.service.providers.ExternalHostsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP data-plane connection hints for well-known discovery (scheme, host, port, API path).
 *
 * <p>When {@link HttpServiceProperties#getExternalHost()} is non-blank and an {@link ExternalHostsProvider}
 * is present, host/port/scheme are taken from {@code mill.application.hosts.externals.&lt;key&gt;} on each
 * access (supports {@code @request.*} placeholders); otherwise defaults match local servlet HTTP.
 */
@Component
@ConditionalOnService(value = "http", group = "data")
public class HttpConnectionDescriptor implements Descriptor {

    private final ExternalHostsProvider externalHosts;
    private final String hostRef;

    /** Base path prefix for data-plane REST endpoints (JSON / protobuf). */
    @JsonProperty("api-path")
    private final String path = "/services/jet/";

    /**
     * @param serviceProperties HTTP service bindings including {@code external-host}
     * @param externalHosts     optional {@link ExternalHostsProvider}
     */
    public HttpConnectionDescriptor(
            HttpServiceProperties serviceProperties,
            @Autowired(required = false) ExternalHostsProvider externalHosts
    ) {
        this.externalHosts = externalHosts;
        this.hostRef = serviceProperties.getExternalHost();
    }

    /** Wire scheme for HTTP data requests (e.g. {@code http}). */
    public String getScheme() {
        ServiceAddressDescriptor external = ExternalHostLookup.resolve(externalHosts, hostRef);
        if (external != null) {
            return external.scheme().toString().toLowerCase();
        }
        return ServiceAddressScheme.HTTP.toString().toLowerCase();
    }

    /** Hostname clients should use for the HTTP data-plane. */
    public String getHost() {
        ServiceAddressDescriptor external = ExternalHostLookup.resolve(externalHosts, hostRef);
        if (external != null) {
            return external.host();
        }
        return "localhost";
    }

    /** TCP port for the HTTP data-plane. */
    public Integer getPort() {
        ServiceAddressDescriptor external = ExternalHostLookup.resolve(externalHosts, hostRef);
        if (external != null && external.port() != null) {
            return external.port();
        }
        return 8080;
    }

    public String getPath() {
        return path;
    }

    /**
     * @return {@link DescriptorTypes#CONNECTIONS_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.CONNECTIONS_TYPE_NAME;
    }
}
