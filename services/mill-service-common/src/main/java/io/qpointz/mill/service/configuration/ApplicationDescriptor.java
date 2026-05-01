package io.qpointz.mill.service.configuration;

import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorSource;
import io.qpointz.mill.service.descriptors.DescriptorTypes;

/**
 * Top-level application identity for the {@code /.well-known/mill} document.
 *
 * <p>Serialized under the {@code app} key by {@link io.qpointz.mill.service.service.WellKnownService} (first
 * {@link Descriptor} with {@link DescriptorTypes#APP_TYPE_NAME}). Transport-specific discovery (gRPC/HTTP
 * surfaces, connection hints, schema names) comes from other {@link Descriptor} beans and
 * {@link DescriptorSource} contributions, not from fields on this type.
 */
public class ApplicationDescriptor implements Descriptor {

    private final String name;

    /**
     * @param name human-readable application name; defaults to {@code "Mill"} when {@code null}
     */
    public ApplicationDescriptor(
            String name
    ) {
        this.name = name == null ? "Mill" : name;
    }

    /**
     * Returns the human-readable application name.
     *
     * @return application name, never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@link DescriptorTypes#APP_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.APP_TYPE_NAME;
    }
}
