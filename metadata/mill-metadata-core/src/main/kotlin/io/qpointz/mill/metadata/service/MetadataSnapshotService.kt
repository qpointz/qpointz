package io.qpointz.mill.metadata.service

import java.io.Writer

/**
 * Exports repository state as canonical multi-document YAML (SPEC §15.5).
 */
interface MetadataSnapshotService {

    /**
     * Writes all entities, facet assignments, facet type definitions, and scopes to [out].
     *
     * @param out UTF-8 writer; caller chooses encoding
     */
    fun snapshotAll(out: Writer)

    /**
     * Writes a subset: the listed entities, their facets, and any definitions and scopes
     * referenced by those facets.
     *
     * @param entityIds entity URNs (canonicalised before lookup)
     * @param out UTF-8 writer; caller chooses encoding
     */
    fun snapshotEntities(entityIds: List<String>, out: Writer)
}
