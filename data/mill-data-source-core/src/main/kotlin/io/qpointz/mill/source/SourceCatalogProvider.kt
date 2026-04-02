package io.qpointz.mill.source

import io.qpointz.mill.source.descriptor.SourceDescriptor

/**
 * Supplies flow (or other) [SourceDescriptor] documents for catalog materialization and metadata inference.
 *
 * The flow backend’s [io.qpointz.mill.data.backend.flow.SourceDefinitionRepository] extends this contract so
 * infrastructure beans can depend on the narrower read surface.
 */
fun interface SourceCatalogProvider {

    /**
     * @return all source descriptors known to this catalog (each [SourceDescriptor.name] must be unique)
     */
    fun getSourceDefinitions(): Iterable<SourceDescriptor>
}
