package io.qpointz.mill.metadata.domain

import java.util.Locale
import kotlin.jvm.JvmStatic

/**
 * Normalisation helpers for Mill URNs (`urn:mill/...`).
 *
 * Relational entity URNs (`urn:mill/metadata/entity:...`) are built in `mill-data-schema-core`
 * via [io.qpointz.mill.data.schema.MetadataEntityUrnCodec].
 */
object MetadataEntityUrn {

    /**
     * @param value raw string from API or storage
     * @return true if non-blank and starts with `urn:mill/` (case-insensitive)
     */
    @JvmStatic
    fun isMillUrn(value: String): Boolean =
        value.trim().startsWith("urn:mill/", ignoreCase = true)

    /**
     * Trims, lowercases the **entire** URN string, and requires a `urn:mill/` prefix.
     *
     * @param urn any Mill URN (entity, scope, facet-type, etc.)
     * @return canonical lowercase form
     * @throws IllegalArgumentException if blank or not a Mill URN
     */
    @JvmStatic
    fun canonicalize(urn: String): String {
        val t = urn.trim().lowercase(Locale.ROOT)
        require(t.isNotEmpty()) { "URN must not be blank" }
        require(t.startsWith("urn:mill/")) { "URN must start with urn:mill/: $t" }
        return t
    }
}
