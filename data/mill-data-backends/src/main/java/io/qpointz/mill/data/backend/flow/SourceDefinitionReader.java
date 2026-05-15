package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.data.backend.resource.BackendResourceLoader;
import io.qpointz.mill.source.descriptor.SourceDescriptor;
import io.qpointz.mill.source.descriptor.SourceObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Utility for reading {@link SourceDescriptor} from YAML files.
 * Centralises access to the Kotlin {@code SourceObjectMapper} singleton.
 */
public final class SourceDefinitionReader {
    private SourceDefinitionReader() {}

    /**
     * Reads a descriptor using the given {@link BackendResourceLoader}.
     *
     * @param loader    loader that resolves {@code location}
     * @param location  Mill resource location string
     * @return parsed descriptor
     * @throws IllegalStateException if parsing fails
     */
    public static SourceDescriptor read(BackendResourceLoader loader, String location) {
        try (InputStream in = loader.open(location)) {
            return SourceObjectMapper.INSTANCE.getYaml().readValue(in, SourceDescriptor.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to parse source descriptor from " + loader.displayLocation(location), e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse source descriptor from " + loader.displayLocation(location), e);
        }
    }

    /**
     * Reads a descriptor from a local filesystem path (compatibility helper).
     *
     * @param path path to YAML
     * @return parsed descriptor
     */
    public static SourceDescriptor read(Path path) {
        try {
            return SourceObjectMapper.INSTANCE.getYaml()
                    .readValue(path.toFile(), SourceDescriptor.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse source descriptor from " + path, e);
        }
    }
}
