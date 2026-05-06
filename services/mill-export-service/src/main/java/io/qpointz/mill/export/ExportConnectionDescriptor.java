package io.qpointz.mill.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import io.qpointz.mill.service.descriptors.ServiceAddressScheme;
import io.qpointz.mill.service.providers.ExternalHostsProvider;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Export HTTP connection hints for well-known discovery (scheme, host, port, API path).
 *
 * <p>When {@link ExportServiceProperties#getExternalHost()} is non-blank and an {@link ExternalHostsProvider}
 * is present, host/port/scheme are taken from {@code mill.application.hosts.externals.&lt;key&gt;}; otherwise defaults
 * match local servlet HTTP.
 */
@Component
@Data
@ConditionalOnService(value = "export", group = "data")
public class ExportConnectionDescriptor implements Descriptor {

    /** Wire scheme for export requests (e.g. {@code http}). */
    @Getter
    private final String scheme;

    /** Hostname clients should use for export HTTP. */
    @Getter
    private final String host;

    /** TCP port for export HTTP. */
    @Getter
    private final Integer port;

    /** Base path prefix for export REST endpoints. */
    @Getter
    @JsonProperty("api-path")
    private final String path = "/services/export/";

    /**
     * @param serviceProperties export bindings including {@code external-host}
     * @param externalHosts     optional {@link ExternalHostsProvider} (usually {@code ServiceAddressProperties})
     */
    public ExportConnectionDescriptor(
            ExportServiceProperties serviceProperties,
            @Autowired(required = false) ExternalHostsProvider externalHosts) {
        val hostRef = serviceProperties.getExternalHost();
        val external = externalHosts != null && hostRef != null && !hostRef.isBlank()
                ? externalHosts.getExternals().getOrDefault(hostRef, null)
                : null;

        if (external != null) {
            this.scheme = external.scheme().toString().toLowerCase();
            this.host = external.host();
            this.port = external.port();
        } else {
            this.scheme = ServiceAddressScheme.HTTP.toString().toLowerCase();
            this.host = "localhost";
            this.port = 8080;
        }
    }

    /**
     * @return {@link DescriptorTypes#CONNECTIONS_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.CONNECTIONS_TYPE_NAME;
    }
}
