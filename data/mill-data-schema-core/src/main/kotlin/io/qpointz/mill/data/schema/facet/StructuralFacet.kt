package io.qpointz.mill.data.schema.facet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.core.TableType
import java.time.Instant

/**
 * Physical storage and schema-related attributes for table/column entities (SPEC §12.5).
 *
 * Schema-layer data shape for JDBC and Calcite bindings; validation is schema/catalog driven.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StructuralFacet(
    val physicalName: String? = null,
    val physicalType: String? = null,
    val precision: Int? = null,
    val scale: Int? = null,
    val nullable: Boolean? = null,
    val isPrimaryKey: Boolean? = null,
    val isForeignKey: Boolean? = null,
    val isUnique: Boolean? = null,
    val backendType: String? = null,
    val tableType: TableType? = null,
    val lastSyncedAt: Instant? = null,
    override val facetType: String = "structural",
) : MetadataFacet
