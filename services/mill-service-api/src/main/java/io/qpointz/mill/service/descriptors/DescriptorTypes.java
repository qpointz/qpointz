package io.qpointz.mill.service.descriptors;

/**
 * Stable {@link Descriptor#getTypeName()} constants shared across descriptor implementations.
 */
public final class DescriptorTypes {

    /** JSON / grouping key for the top-level application identity descriptor. */
    public static final String APP_TYPE_NAME = "app";

    private DescriptorTypes() {
        // Utility holder
    }

    /** Grouping key for service advertisements ({@code GrpcServiceDescriptor}, {@code HttpServiceDescriptor}, …). */
    public static final String SERVICE_TYPE_NAME = "services";

    /** Grouping key for physical schema name entries (often from {@link io.qpointz.mill.autoconfigure.data.backend.SchemaDescriptorsSource}). */
    public static final String SCHEMA_TYPE_NAME = "schemas";

    /** Type name for {@link SecurityDescriptor}. */
    public static final String SECURITY_TYPE_NAME = "security";

    /** Type name for {@link AuthMethodDescriptor} entries nested under security. */
    public static final String AUTH_METHODS_TYPE_NAME = "auth";

    /** Grouping key for concrete connection hints (gRPC / HTTP host, port, scheme, paths). */
    public static final String CONNECTIONS_TYPE_NAME = "connections";
}
