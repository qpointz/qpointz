package io.qpointz.mill.metadata.domain

import java.io.Serializable
import java.time.Instant

/** Descriptor defining lifecycle, applicability, and validation for a facet type. */
data class FacetTypeDescriptor(
    var typeKey: String = "",
    var mandatory: Boolean = false,
    var enabled: Boolean = true,
    var displayName: String? = null,
    var description: String? = null,
    var applicableTo: Set<MetadataTargetType>? = null,
    var version: String? = null,
    var contentSchema: Map<String, Any?>? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null
) : Serializable {

    /** Checks whether the descriptor allows attachment to the given target type. */
    fun isApplicableTo(targetType: MetadataTargetType): Boolean {
        val targets = applicableTo
        if (targets == null || targets.isEmpty() || MetadataTargetType.ANY in targets) return true
        return targetType in targets
    }

    /** Indicates whether this descriptor defines JSON-schema-like content rules. */
    fun hasContentSchema(): Boolean =
        contentSchema != null && contentSchema!!.isNotEmpty()

    /** Builder entry point kept for Java interop call sites. */
    companion object {
        private const val serialVersionUID: Long = 1L

        @JvmStatic
        fun builder() = Builder()
    }

    /** Java-friendly builder for creating [FacetTypeDescriptor] instances. */
    class Builder {
        private var typeKey: String = ""
        private var mandatory: Boolean = false
        private var enabled: Boolean = true
        private var displayName: String? = null
        private var description: String? = null
        private var applicableTo: Set<MetadataTargetType>? = null
        private var version: String? = null
        private var contentSchema: Map<String, Any?>? = null
        private var createdAt: Instant? = null
        private var updatedAt: Instant? = null
        private var createdBy: String? = null
        private var updatedBy: String? = null

        fun typeKey(v: String) = apply { typeKey = v }
        fun mandatory(v: Boolean) = apply { mandatory = v }
        fun enabled(v: Boolean) = apply { enabled = v }
        fun displayName(v: String?) = apply { displayName = v }
        fun description(v: String?) = apply { description = v }
        fun applicableTo(v: Set<MetadataTargetType>?) = apply { applicableTo = v }
        fun version(v: String?) = apply { version = v }
        fun contentSchema(v: Map<String, Any?>?) = apply { contentSchema = v }
        fun createdAt(v: Instant?) = apply { createdAt = v }
        fun updatedAt(v: Instant?) = apply { updatedAt = v }
        fun createdBy(v: String?) = apply { createdBy = v }
        fun updatedBy(v: String?) = apply { updatedBy = v }

        fun build() = FacetTypeDescriptor(
            typeKey, mandatory, enabled, displayName, description,
            applicableTo, version, contentSchema, createdAt, updatedAt, createdBy, updatedBy
        )
    }
}
