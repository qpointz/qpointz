package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.source.descriptor.SourceDescriptor;
import io.qpointz.mill.source.descriptor.SourceObjectMapper;

import java.nio.file.Path;

/**
 * Utility for reading {@link SourceDescriptor} from YAML files.
 * Centralises access to the Kotlin {@code SourceObjectMapper} singleton.
 */
public final class SourceDefinitionReader {
    private SourceDefinitionReader() {}

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
