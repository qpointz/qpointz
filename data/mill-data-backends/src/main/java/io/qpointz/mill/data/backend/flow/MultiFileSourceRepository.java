package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.source.descriptor.SourceDescriptor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.*;

public class MultiFileSourceRepository implements SourceDefinitionRepository {

    @Getter
    private final List<Path> descriptorPaths;

    public MultiFileSourceRepository(List<Path> descriptorPaths) {
        this.descriptorPaths = List.copyOf(descriptorPaths);
    }

    public MultiFileSourceRepository(Path... descriptorPaths) {
        this(List.of(descriptorPaths));
    }

    @Override
    public Iterable<SourceDescriptor> getSourceDefinitions() {
        var descriptors = new ArrayList<SourceDescriptor>();
        var names = new HashSet<String>();
        for (var path : descriptorPaths) {
            try {
                var descriptor = SourceDefinitionReader.read(path);
                if (!names.add(descriptor.getName())) {
                    throw new IllegalStateException(
                            "Duplicate source name '" + descriptor.getName()
                                    + "' from " + path);
                }
                descriptors.add(descriptor);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to read source descriptor from " + path, e);
            }
        }
        return Collections.unmodifiableList(descriptors);
    }
}
