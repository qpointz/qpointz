package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Well-known advertisement for the streaming export HTTP surface ({@link DescriptorTypes#SERVICE_TYPE_NAME}).
 *
 * <p>Binds {@link ExportServiceProperties} for {@code mill.data.services.export.*};
 * see {@link ExportConnectionDescriptor} for host/port/scheme in the
 * {@link DescriptorTypes#CONNECTIONS_TYPE_NAME} bucket.
 */
@Data
@Component
@ConditionalOnService(value = "export", group = "data")
@EnableConfigurationProperties(ExportServiceProperties.class)
public class ExportServiceDescriptor implements Descriptor {

    /** Logical service id in discovery JSON (alongside {@code data-http}, {@code data-grpc}). */
    @Getter
    private final String name = "data-export";

    /** Creates the descriptor; connection details are supplied by {@link ExportConnectionDescriptor}. */
    public ExportServiceDescriptor() {
    }

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.SERVICE_TYPE_NAME;
    }
}
