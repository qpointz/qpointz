package io.qpointz.mill.metadata.repository

/**
 * Full facet assignment persistence: [FacetReadSide] + [FacetWriteSide].
 */
interface FacetRepository : FacetReadSide, FacetWriteSide
