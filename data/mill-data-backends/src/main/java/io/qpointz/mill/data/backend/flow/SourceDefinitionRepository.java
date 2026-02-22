package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.source.descriptor.SourceDescriptor;

/**
 * Provides source definitions to the flow backend.
 *
 * <p>Implementations may read from YAML files, a database, a directory scan,
 * or any other storage. Each returned {@link SourceDescriptor} becomes a
 * Calcite schema whose name is the descriptor's {@code name} property.</p>
 */
public interface SourceDefinitionRepository {
    Iterable<SourceDescriptor> getSourceDefinitions();
}
