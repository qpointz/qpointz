package io.qpointz.mill.metadata.domain.facet.exceptions

import io.qpointz.mill.MillException

/** Thrown when a facet type manifest cannot be parsed or fails strict structural checks. */
class FacetTypeManifestInvalidException(message: String, cause: Throwable? = null) : MillException(message, cause)
