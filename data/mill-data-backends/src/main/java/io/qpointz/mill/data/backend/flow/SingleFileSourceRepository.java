package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.source.descriptor.SourceDescriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
public class SingleFileSourceRepository implements SourceDefinitionRepository {

    @Getter
    private final Path descriptorPath;

    @Override
    public Iterable<SourceDescriptor> getSourceDefinitions() {
        try {
            var descriptor = SourceDefinitionReader.read(descriptorPath);
            return List.of(descriptor);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to read source descriptor from " + descriptorPath, e);
        }
    }
}
