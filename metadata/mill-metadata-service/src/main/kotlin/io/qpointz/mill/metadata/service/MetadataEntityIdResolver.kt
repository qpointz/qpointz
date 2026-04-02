package io.qpointz.mill.metadata.service

import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MillUrn
import java.util.Locale

/**
 * Resolves REST and legacy catalog keys to canonical metadata entity instance URNs.
 *
 * Accepts three input forms:
 * - Any typed Mill URN (`urn:mill/…`) — returned canonicalised as-is.
 * - Legacy dot-separated catalog key (`schema[.table[.column]]`) — expanded via [codec].
 *
 * @see MetadataEntityUrnCodec
 */
object MetadataEntityIdResolver {

    /**
     * @param raw any Mill URN or legacy `schema[.table[.column]]` key
     * @param codec relational URN codec used to expand legacy keys
     * @return canonical URN string
     */
    fun resolve(raw: String, codec: MetadataEntityUrnCodec): String {
        val t = raw.trim()
        if (MetadataEntityUrn.isMillUrn(t)) {
            return MetadataEntityUrn.canonicalize(t)
        }
        val parts = t.lowercase(Locale.ROOT)
            .split('.').map { it.trim() }.filter { it.isNotEmpty() }
        require(parts.isNotEmpty()) { "Invalid metadata entity id: $raw" }
        return when (parts.size) {
            1 -> codec.forSchema(parts[0])
            2 -> codec.forTable(parts[0], parts[1])
            else -> codec.forAttribute(parts[0], parts[1], parts.drop(2).joinToString("."))
        }
    }

    /**
     * Normalises an entity id to a lowercase dot-separated catalog key for physical schema checks.
     *
     * For typed Mill URNs the local `id` segment (after the final `:`) is returned.
     * For legacy dot-separated keys the lowercased input is returned unchanged.
     *
     * @param raw any Mill URN or legacy `schema[.table[.column]]` key
     * @return dot-separated local id in lowercase
     */
    fun canonicalizeEntityId(raw: String): String {
        val t = raw.trim()
        val parsed = MillUrn.parse(t)
        if (parsed != null) {
            return parsed.id
        }
        return t.lowercase(Locale.ROOT)
    }
}
