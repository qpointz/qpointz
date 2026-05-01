package io.qpointz.mill.data.backend.access.http.configuration;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Well-known advertisement for the Mill HTTP data-plane ({@link DescriptorTypes#SERVICE_TYPE_NAME} bucket).
 *
 * <p>Binds {@link HttpServiceProperties} for {@code mill.data.services.http.*}; see {@link HttpConnectionDescriptor}
 * for resolved host/port/scheme in the {@link DescriptorTypes#CONNECTIONS_TYPE_NAME} bucket.
 */
@Data
@Component
@ConditionalOnService(value = "http", group = "data")
@EnableConfigurationProperties(HttpServiceProperties.class)
public class HttpServiceDescriptor implements Descriptor {

    /** Logical service id in discovery JSON. */
    @Getter
    private final String name = "data-http";

    /** Creates the descriptor; HTTP connection details are supplied by {@link HttpConnectionDescriptor}. */
    public HttpServiceDescriptor() {
    }

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.SERVICE_TYPE_NAME;
    }
}
