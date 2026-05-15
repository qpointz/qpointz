package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.data.backend.resource.BackendResourceLoader;
import io.qpointz.mill.source.descriptor.SourceDescriptor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Single-descriptor {@link SourceDefinitionRepository} backed by a {@link BackendResourceLoader}.
 */
public class SingleFileSourceRepository implements SourceDefinitionRepository {

    private final BackendResourceLoader resourceLoader;

    @Getter
    private final String descriptorLocation;

    /**
     * @param resourceLoader loader used to open {@code descriptorLocation}
     * @param descriptorLocation Mill resource location for the YAML descriptor
     */
    public SingleFileSourceRepository(BackendResourceLoader resourceLoader, String descriptorLocation) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
        this.descriptorLocation = Objects.requireNonNull(descriptorLocation, "descriptorLocation");
    }

    /**
     * Compatibility constructor for a local filesystem path.
     *
     * @param descriptorPath path to the descriptor YAML file
     */
    public SingleFileSourceRepository(Path descriptorPath) {
        this(
                new LocalPathBackendResourceLoader(List.of(descriptorPath)),
                descriptorPath.toAbsolutePath().normalize().toUri().toString());
    }

    @Override
    public Iterable<SourceDescriptor> getSourceDefinitions() {
        try {
            var descriptor = SourceDefinitionReader.read(resourceLoader, descriptorLocation);
            return List.of(descriptor);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to read source descriptor from " + resourceLoader.displayLocation(descriptorLocation), e);
        }
    }
}
