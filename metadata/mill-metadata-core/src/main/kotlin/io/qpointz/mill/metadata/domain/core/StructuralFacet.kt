package io.qpointz.mill.metadata.domain.core

import io.qpointz.mill.metadata.domain.AbstractFacet
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.ValidationException
import java.time.Instant

/** Physical storage and schema-related attributes for table/column entities. */
open class StructuralFacet(
    var physicalName: String? = null,
    var physicalType: String? = null,
    var precision: Int? = null,
    var scale: Int? = null,
    var nullable: Boolean? = null,
    var isPrimaryKey: Boolean? = null,
    var isForeignKey: Boolean? = null,
    var isUnique: Boolean? = null,
    var backendType: String? = null,
    var tableType: TableType? = null,
    var lastSyncedAt: Instant? = null
) : AbstractFacet() {

    override val facetType: String get() = "structural"

    override fun validate() {
        if (physicalName.isNullOrEmpty()) {
            throw ValidationException("StructuralFacet: physicalName is required")
        }
    }

    override fun merge(other: MetadataFacet): MetadataFacet {
        if (other !is StructuralFacet) return this
        other.physicalName?.let { physicalName = it }
        other.physicalType?.let { physicalType = it }
        other.precision?.let { precision = it }
        other.scale?.let { scale = it }
        other.nullable?.let { nullable = it }
        other.isPrimaryKey?.let { isPrimaryKey = it }
        other.isForeignKey?.let { isForeignKey = it }
        other.isUnique?.let { isUnique = it }
        other.backendType?.let { backendType = it }
        other.tableType?.let { tableType = it }
        other.lastSyncedAt?.let { lastSyncedAt = it }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructuralFacet) return false
        return physicalName == other.physicalName && physicalType == other.physicalType
    }

    override fun hashCode(): Int = (physicalName?.hashCode() ?: 0) * 31 + (physicalType?.hashCode() ?: 0)
}
