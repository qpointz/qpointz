package io.qpointz.mill.service.descriptors;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ApplicationDescriptor {

    public record SchemaDescriptor(String name, URI link) {}

    private final Collection<ServiceDescriptor> services;
    private final SecurityDescriptor security;
    private final Map<String, SchemaDescriptor> schemas;

    public ApplicationDescriptor(
            Collection<ServiceDescriptor> services,
            SecurityDescriptor security,
            Map<String, SchemaDescriptor> schemas
    ) {
        this.services = services == null ? List.of() : services;
        this.security = security;
        this.schemas = schemas == null ? Map.of() : schemas;
    }

    public Collection<ServiceDescriptor> getServices() {
        return services;
    }

    public SecurityDescriptor getSecurity() {
        return security;
    }

    public Map<String, SchemaDescriptor> getSchemas() {
        return schemas;
    }
}
