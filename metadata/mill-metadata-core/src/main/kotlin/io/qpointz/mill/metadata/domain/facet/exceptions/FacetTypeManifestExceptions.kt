package io.qpointz.mill.metadata.domain.facet.exceptions

import io.qpointz.mill.MillException

/** Thrown when a facet type manifest cannot be parsed or fails strict structural checks. */
class FacetTypeManifestInvalidException(message: String, cause: Throwable? = null) : MillException(message, cause)

/** Thrown when a facet type operation conflicts with an existing catalog state. */
class FacetTypeConflictException(message: String, cause: Throwable? = null) : MillException(message, cause)

/** Thrown when a requested facet type does not exist in the catalog. */
class FacetTypeNotFoundException(message: String, cause: Throwable? = null) : MillException(message, cause)

