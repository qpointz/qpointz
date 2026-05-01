package io.qpointz.mill.autoconfigure.data.backend;

import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorSource;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.StreamSupport;

/**
 * Publishes Calcite-backed schema names into the well-known descriptor map so clients can enumerate catalogs.
 *
 * <p>Each schema name from {@link SchemaProvider} becomes a nested {@link SchemaDescriptor} record whose
 * {@link Descriptor#getTypeName()} is {@value io.qpointz.mill.service.descriptors.DescriptorTypes#SCHEMA_TYPE_NAME}.
 */
@Component
public class SchemaDescriptorsSource implements DescriptorSource {

    private final SchemaProvider schemaProvider;

    /**
     * @param schemaProvider runtime provider of registered schema names
     */
    public SchemaDescriptorsSource(SchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
    }

    /**
     * Logical group label for this {@link DescriptorSource}; matches descriptor {@link Descriptor#getTypeName()}.
     *
     * @return {@value io.qpointz.mill.service.descriptors.DescriptorTypes#SCHEMA_TYPE_NAME}
     */
    @Override
    public String getGroupName() {
        return DescriptorTypes.SCHEMA_TYPE_NAME;
    }

    /**
     * Materializes one descriptor per schema name currently known to the data plane.
     *
     * @return non-null collection (possibly empty)
     */
    @Override
    public Collection<Descriptor> getDescriptors() {
        return StreamSupport.stream(this.schemaProvider.getSchemaNames().spliterator(), false)
                .map(k -> (Descriptor) schemaDescriptor(k))
                .toList();
    }

    /**
     * Minimal schema entry for discovery (name only; optional URI lives on the public API record when used).
     *
     * @param name physical schema name
     */
    public record SchemaDescriptor(String name) implements Descriptor {

        @Override
        public String getTypeName() {
            return DescriptorTypes.SCHEMA_TYPE_NAME;
        }
    }

    /**
     * @param k raw schema name from the provider
     * @return descriptor wrapper for well-known aggregation
     */
    private SchemaDescriptor schemaDescriptor(String k) {
        return new SchemaDescriptor(k);
    }
}
