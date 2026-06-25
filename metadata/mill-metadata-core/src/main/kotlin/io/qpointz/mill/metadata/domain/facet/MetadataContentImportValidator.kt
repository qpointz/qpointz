package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Validates {@link MetadataContent} rows during YAML seed import.
 */
object MetadataContentImportValidator {

    /**
     * @param content category guidance row
     * @throws IllegalArgumentException when body is invalid
     */
    fun validateCategoryContent(content: MetadataContent) {
        require(content.contentKind == MetadataContent.KIND_FACET_TYPE_CATEGORY) {
            "Expected contentKind=${MetadataContent.KIND_FACET_TYPE_CATEGORY}"
        }
        val slug = categorySlug(content.targetUrn)
        val body = parseJsonObject(content.contentBody)
        val category = body["category"]?.toString()?.trim()
            ?: error("facet-type-category body missing 'category' (${content.contentUrn})")
        require(category == slug) {
            "facet-type-category slug '$slug' does not match body category '$category' (${content.contentUrn})"
        }
    }

    /**
     * @param content example row
     * @param knownFacetTypeUrns facet types available from catalog or the same import batch
     * @throws IllegalArgumentException when body or target type is invalid
     */
    fun validateExampleContent(content: MetadataContent, knownFacetTypeUrns: Set<String>) {
        require(content.contentKind == MetadataContent.KIND_FACET_TYPE_EXAMPLE) {
            "Expected contentKind=${MetadataContent.KIND_FACET_TYPE_EXAMPLE}"
        }
        val target = content.targetUrn.trim()
        require(target in knownFacetTypeUrns) {
            "facet-type-example target '$target' is not a known facet type (${content.contentUrn})"
        }
        val body = parseJsonObject(content.contentBody)
        val payload = body["payload"] as? Map<*, *>
            ?: error("facet-type-example body missing 'payload' object (${content.contentUrn})")
        require(payload.isNotEmpty()) {
            "facet-type-example payload must not be empty (${content.contentUrn})"
        }
    }

    private fun categorySlug(targetUrn: String): String {
        val prefix = MetadataUrns.FACET_TYPE_CATEGORY_PREFIX
        require(targetUrn.startsWith(prefix)) {
            "facet-type-category targetUrn must start with $prefix: $targetUrn"
        }
        return targetUrn.removePrefix(prefix)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseJsonObject(contentBody: String): Map<String, Any?> {
        val mapper = io.qpointz.mill.utils.JsonUtils.defaultJsonMapper()
        return mapper.readValue(contentBody, Map::class.java) as Map<String, Any?>
    }
}
