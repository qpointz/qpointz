package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.data.backend.resource.BackendResourceLoader;
import io.qpointz.mill.source.descriptor.SourceDescriptor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.*;

/**
 * Loads {@link SourceDescriptor}s from configured resource locations using a {@link BackendResourceLoader}.
 */
public class MultiFileSourceRepository implements SourceDefinitionRepository {

    private final BackendResourceLoader resourceLoader;

    @Getter
    private final List<String> descriptorLocations;

    /**
     * @param resourceLoader loader used to open each configured location
     * @param descriptorLocations Mill resource locations (for example {@code classpath:...}, {@code file:...}, or bare paths)
     */
    public MultiFileSourceRepository(BackendResourceLoader resourceLoader, List<String> descriptorLocations) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
        this.descriptorLocations = List.copyOf(Objects.requireNonNull(descriptorLocations, "descriptorLocations"));
    }

    /**
     * @param resourceLoader loader used to open each configured location
     * @param descriptorLocations Mill resource locations
     */
    public MultiFileSourceRepository(BackendResourceLoader resourceLoader, String... descriptorLocations) {
        this(resourceLoader, List.of(descriptorLocations));
    }

    /**
     * Compatibility constructor for local filesystem paths (treated as {@code file:} resources).
     *
     * @param descriptorPaths local paths to descriptor YAML files
     */
    public MultiFileSourceRepository(List<Path> descriptorPaths) {
        this(new LocalPathBackendResourceLoader(descriptorPaths), toFileUriStrings(descriptorPaths));
    }

    /**
     * @param descriptorPaths local paths to descriptor YAML files
     */
    public MultiFileSourceRepository(Path... descriptorPaths) {
        this(List.of(descriptorPaths));
    }

    private static List<String> toFileUriStrings(List<Path> paths) {
        return paths.stream().map(p -> p.toAbsolutePath().normalize().toUri().toString()).toList();
    }

    public Iterable<SourceDescriptor> getSourceDefinitions() {
        var descriptors = new ArrayList<SourceDescriptor>();
        var names = new HashSet<String>();
        for (var loc : descriptorLocations) {
            try {
                var descriptor = SourceDefinitionReader.read(resourceLoader, loc);
                if (!names.add(descriptor.getName())) {
                    throw new IllegalStateException(
                            "Duplicate source name '" + descriptor.getName()
                                    + "' from " + resourceLoader.displayLocation(loc));
                }
                descriptors.add(descriptor);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to read source descriptor from " + resourceLoader.displayLocation(loc), e);
            }
        }
        return Collections.unmodifiableList(descriptors);
    }
}
