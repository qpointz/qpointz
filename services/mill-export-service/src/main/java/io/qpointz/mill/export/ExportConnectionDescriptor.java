package io.qpointz.mill.export;

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
 * Export HTTP connection hints for well-known discovery (scheme, host, port, API path).
 *
 * <p>When {@link ExportServiceProperties#getExternalHost()} is non-blank and an {@link ExternalHostsProvider}
 * is present, host/port/scheme are taken from {@code mill.application.hosts.externals.&lt;key&gt;} on each
 * access (supports {@code @request.*} placeholders); otherwise defaults match local servlet HTTP.
 */
@Component
@ConditionalOnService(value = "export", group = "data")
public class ExportConnectionDescriptor implements Descriptor {

    private final ExternalHostsProvider externalHosts;
    private final String hostRef;

    /** Base path prefix for export REST endpoints. */
    @JsonProperty("api-path")
    private final String path = "/services/export/";

    /**
     * @param serviceProperties export bindings including {@code external-host}
     * @param externalHosts     optional {@link ExternalHostsProvider}
     */
    public ExportConnectionDescriptor(
            ExportServiceProperties serviceProperties,
            @Autowired(required = false) ExternalHostsProvider externalHosts
    ) {
        this.externalHosts = externalHosts;
        this.hostRef = serviceProperties.getExternalHost();
    }

    /** Wire scheme for export requests (e.g. {@code http}). */
    public String getScheme() {
        ServiceAddressDescriptor external = ExternalHostLookup.resolve(externalHosts, hostRef);
        if (external != null) {
            return external.scheme().toString().toLowerCase();
        }
        return ServiceAddressScheme.HTTP.toString().toLowerCase();
    }

    /** Hostname clients should use for export HTTP. */
    public String getHost() {
        ServiceAddressDescriptor external = ExternalHostLookup.resolve(externalHosts, hostRef);
        if (external != null) {
            return external.host();
        }
        return "localhost";
    }

    /** TCP port for export HTTP. */
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
