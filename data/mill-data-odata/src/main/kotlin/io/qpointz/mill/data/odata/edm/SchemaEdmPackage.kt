package io.qpointz.mill.data.odata.edm

import com.sdl.odata.api.edm.model.EntityDataModel
import io.qpointz.mill.data.odata.annotation.EdmAnnotationModel

/**
 * RWS entity data model plus CSDL annotations derived from metadata facets.
 *
 * @property entityDataModel programmatic EDM consumed by the RWS OData stack
 * @property annotations facet-derived CSDL annotations for {@code $metadata} enhancement
 */
data class SchemaEdmPackage(
    val entityDataModel: EntityDataModel,
    val annotations: EdmAnnotationModel,
)
