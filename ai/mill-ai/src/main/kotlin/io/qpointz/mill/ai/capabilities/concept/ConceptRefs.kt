package io.qpointz.mill.ai.capabilities.concept

import io.qpointz.mill.data.metadata.ModelEntityUrn
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MillUrn

/**
 * Resolves business concept references (`urn:mill/model/concept:<slug>`) for the concept capability.
 *
 * Do not use [io.qpointz.mill.ai.capabilities.metadata.MetadataEntityIds] for concepts — physical
 * catalog paths and concept logical refs are separate resolution domains.
 */
object ConceptRefs {

    /**
     * @param raw full concept URN (`urn:mill/model/concept:<slug>`)
     * @return canonical concept URN
     * @throws IllegalArgumentException when [raw] is blank or not a concept URN
     */
    fun parse(raw: String): String {
        val trimmed = raw.trim()
        require(trimmed.isNotEmpty()) { "concept ref must not be blank" }
        val canonical = if (MetadataEntityUrn.isMillUrn(trimmed)) {
            MetadataEntityUrn.canonicalize(trimmed)
        } else {
            throw IllegalArgumentException("concept ref must be a full URN urn:mill/model/concept:<slug>, got: $raw")
        }
        require(ModelEntityUrn.isConceptUrn(canonical)) {
            "not a concept URN: $canonical"
        }
        return canonical
    }

    /**
     * @param conceptRef canonical concept URN
     * @return kebab-case slug segment
     */
    fun slugFromRef(conceptRef: String): String {
        val canonical = parse(conceptRef)
        return MillUrn.parse(canonical)?.id
            ?: throw IllegalArgumentException("cannot extract slug from concept ref: $conceptRef")
    }

    /**
     * Builds the canonical concept URN for a slug.
     *
     * @param slug kebab-case concept identifier
     */
    fun refFromSlug(slug: String): String = ModelEntityUrn.forConcept(normalizeSlug(slug))

    /**
     * Normalizes a human concept name to a kebab-case slug.
     *
     * @param name display name such as `VIP Passengers`
     */
    fun slugFromName(name: String): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "concept name must not be blank" }
        return normalizeSlug(trimmed)
    }

    private fun normalizeSlug(raw: String): String {
        val slug = raw.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .replace(Regex("-+"), "-")
        require(slug.isNotEmpty()) { "cannot derive slug from: $raw" }
        return slug
    }
}
