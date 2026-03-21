package io.qpointz.mill.service.descriptors;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Describes the top-level application identity and its registered services and schemas.
 *
 * <p>This is a pure value type passed to well-known endpoints so clients can discover the
 * application name, available services, and schema locations without prior knowledge.
 * All fields default to safe empty values if {@code null} is supplied at construction time.
 */
public class ApplicationDescriptor {

    private final String name;
    private final Collection<ServiceDescriptor> services;
    private final Map<String, SchemaDescriptor> schemas;

    /**
     * Creates an {@code ApplicationDescriptor}.
     *
     * @param name             human-readable application name; defaults to {@code "Mill"} when {@code null}
     * @param services         registered service descriptors; defaults to an empty list when {@code null}
     * @param schemas          registered schema descriptors keyed by schema name; defaults to an empty map when {@code null}
     */
    public ApplicationDescriptor(
            String name,
            Collection<ServiceDescriptor> services,
            Map<String, SchemaDescriptor> schemas
    ) {
        this.name = name == null ? "Mill" : name;
        this.services = services == null ? List.of() : services;
        this.schemas = schemas == null ? Map.of() : schemas;
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
     * Returns the registered service descriptors.
     *
     * @return unmodifiable collection of {@link ServiceDescriptor} instances
     */
    public Collection<ServiceDescriptor> getServices() {
        return services;
    }

    /**
     * Returns the registered schema descriptors keyed by schema name.
     *
     * @return unmodifiable map of schema name to {@link SchemaDescriptor}
     */
    public Map<String, SchemaDescriptor> getSchemas() {
        return schemas;
    }
}
