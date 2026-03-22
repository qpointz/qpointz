package io.qpointz.mill.metadata.domain

import java.io.Serializable
import java.time.Instant

/**
 * Descriptor defining lifecycle, applicability, and validation for a facet type.
 *
 * @property typeKey      unique identifier for this facet type; should be a Mill URN (e.g.
 *                        `urn:mill/metadata/facet-type:descriptive`).
 * @property mandatory    whether the facet type is required on applicable entities.
 * @property enabled      whether this facet type is active and visible.
 * @property displayName  human-readable label.
 * @property description  optional longer description.
 * @property applicableTo set of entity-type URN strings this facet type may be attached to;
 *                        `null` or empty means applicable to all entity types.
 * @property version      optional schema version string.
 * @property contentSchema optional JSON-schema-like validation rules for facet payloads.
 * @property createdAt    creation timestamp.
 * @property updatedAt    last-modified timestamp.
 * @property createdBy    actor who created the descriptor.
 * @property updatedBy    actor who last modified the descriptor.
 */
data class FacetTypeDescriptor(
    var typeKey: String = "",
    var mandatory: Boolean = false,
    var enabled: Boolean = true,
    var displayName: String? = null,
    var description: String? = null,
    var applicableTo: Set<String>? = null,
    var version: String? = null,
    var contentSchema: Map<String, Any?>? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null
) : Serializable {

    /**
     * Checks whether the descriptor allows attachment to the given target entity type.
     *
     * Returns `true` when [applicableTo] is null or empty (meaning "applies to all"),
     * or when [targetType] is present in [applicableTo].
     *
     * @param targetType entity-type string (e.g. `urn:mill/metadata/entity-type:table`)
     * @return `true` if this facet type may be used on entities of [targetType]
     */
    fun isApplicableTo(targetType: String): Boolean {
        val targets = applicableTo
        if (targets.isNullOrEmpty()) return true
        return targetType in targets
    }

    /** Indicates whether this descriptor defines JSON-schema-like content rules. */
    fun hasContentSchema(): Boolean =
        contentSchema != null && contentSchema!!.isNotEmpty()

    /** Builder entry point kept for Java interop call sites. */
    companion object {
        private const val serialVersionUID: Long = 1L

        /** Creates a new [Builder] for constructing a [FacetTypeDescriptor]. */
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
        private var applicableTo: Set<String>? = null
        private var version: String? = null
        private var contentSchema: Map<String, Any?>? = null
        private var createdAt: Instant? = null
        private var updatedAt: Instant? = null
        private var createdBy: String? = null
        private var updatedBy: String? = null

        /** Sets the [FacetTypeDescriptor.typeKey]. */
        fun typeKey(v: String) = apply { typeKey = v }

        /** Sets the [FacetTypeDescriptor.mandatory] flag. */
        fun mandatory(v: Boolean) = apply { mandatory = v }

        /** Sets the [FacetTypeDescriptor.enabled] flag. */
        fun enabled(v: Boolean) = apply { enabled = v }

        /** Sets the [FacetTypeDescriptor.displayName]. */
        fun displayName(v: String?) = apply { displayName = v }

        /** Sets the [FacetTypeDescriptor.description]. */
        fun description(v: String?) = apply { description = v }

        /**
         * Sets the [FacetTypeDescriptor.applicableTo] set of entity-type URN strings.
         *
         * @param v set of entity-type URN strings, or null for "applies to all"
         */
        fun applicableTo(v: Set<String>?) = apply { applicableTo = v }

        /** Sets the [FacetTypeDescriptor.version]. */
        fun version(v: String?) = apply { version = v }

        /** Sets the [FacetTypeDescriptor.contentSchema]. */
        fun contentSchema(v: Map<String, Any?>?) = apply { contentSchema = v }

        /** Sets the [FacetTypeDescriptor.createdAt] timestamp. */
        fun createdAt(v: Instant?) = apply { createdAt = v }

        /** Sets the [FacetTypeDescriptor.updatedAt] timestamp. */
        fun updatedAt(v: Instant?) = apply { updatedAt = v }

        /** Sets the [FacetTypeDescriptor.createdBy] actor. */
        fun createdBy(v: String?) = apply { createdBy = v }

        /** Sets the [FacetTypeDescriptor.updatedBy] actor. */
        fun updatedBy(v: String?) = apply { updatedBy = v }

        /** Builds and returns the [FacetTypeDescriptor]. */
        fun build() = FacetTypeDescriptor(
            typeKey, mandatory, enabled, displayName, description,
            applicableTo, version, contentSchema, createdAt, updatedAt, createdBy, updatedBy
        )
    }
}
