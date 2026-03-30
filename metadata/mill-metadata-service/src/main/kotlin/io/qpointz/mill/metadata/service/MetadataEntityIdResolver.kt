package io.qpointz.mill.metadata.service

import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import java.util.Locale

/**
 * Resolves REST and legacy catalog keys to canonical metadata entity instance URNs.
 *
 * @see MetadataEntityUrnCodec
 */
object MetadataEntityIdResolver {

    /**
     * @param raw entity URN, legacy `schema[.table[.column]]` key, or case-variant thereof
     * @param codec relational URN codec
     * @return canonical `urn:mill/metadata/entity:…` string
     */
    fun resolve(raw: String, codec: MetadataEntityUrnCodec): String {
        val t = raw.trim()
        if (MetadataEntityUrn.isMillUrn(t)) {
            return MetadataEntityUrn.canonicalize(t)
        }
        val local = extractLocalPart(t)
        val parts = local.split('.').map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotEmpty() }
        require(parts.isNotEmpty()) { "Invalid metadata entity id: $raw" }
        return when (parts.size) {
            1 -> codec.forSchema(parts[0])
            2 -> codec.forTable(parts[0], parts[1])
            else -> codec.forAttribute(parts[0], parts[1], parts.drop(2).joinToString("."))
        }
    }

    private fun extractLocalPart(t: String): String {
        val prefix = "urn:mill/metadata/entity:"
        if (t.startsWith(prefix, ignoreCase = true)) {
            return t.substring(prefix.length)
        }
        return t.lowercase(Locale.ROOT)
    }

    /**
     * Normalises an entity id to a lowercase dot-separated catalog key for physical schema checks.
     *
     * @param raw entity URN (`urn:mill/metadata/entity:…`) or legacy `schema[.table[.column]]` key
     * @return local id segment in lowercase (without the `urn:mill/metadata/entity:` prefix when a URN)
     */
    fun canonicalizeEntityId(raw: String): String {
        val t = raw.trim()
        val prefix = "urn:mill/metadata/entity:"
        if (t.startsWith(prefix, ignoreCase = true)) {
            return MetadataEntityUrn.canonicalize(t).substring(prefix.length)
        }
        return t.lowercase(Locale.ROOT)
    }
}
