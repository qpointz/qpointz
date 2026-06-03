package io.qpointz.mill.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds {@code mill.application.hosts.externals.*} before placeholder resolution
 * ({@link ServiceAddressPlaceholderResolver}).
 */
@ConfigurationProperties(prefix = "mill.application.hosts")
public class ServiceAddressProperties {

    /**
     * Raw endpoint fields from configuration. Values may be literals or {@code @request.*} placeholders
     * (quote in YAML when needed, e.g. {@code host: "@request.host"}).
     *
     * @param scheme wire scheme (e.g. {@code http}, {@code grpc}, or {@code @request.scheme})
     * @param host   hostname or {@code @request.host}
     * @param port   port number as string (e.g. {@code 9090} or {@code @request.port})
     */
    public record ServiceEndpointDescriptor(
            String scheme,
            String host,
            String port
    ) {
    }

    private Map<String, ServiceEndpointDescriptor> externals = new HashMap<>();

    /**
     * @return configured external endpoints keyed by logical name (e.g. {@code http}, {@code grpc-request})
     */
    public Map<String, ServiceEndpointDescriptor> getExternals() {
        return externals;
    }

    /**
     * @param externals keyed endpoint definitions from application configuration
     */
    public void setExternals(Map<String, ServiceEndpointDescriptor> externals) {
        this.externals = externals == null ? new HashMap<>() : externals;
    }
}
