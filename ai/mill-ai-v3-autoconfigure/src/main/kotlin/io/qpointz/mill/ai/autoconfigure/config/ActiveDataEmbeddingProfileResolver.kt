package io.qpointz.mill.ai.autoconfigure.config

import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatProperties

/**
 * Resolves the active `mill.ai.data.embedding` profile from `mill.ai.chat.value-mapping.embedding`.
 */
class ActiveDataEmbeddingProfileResolver(
    private val chat: AiV3ChatProperties,
    private val data: DataEmbeddingConfigurationProperties,
) {

    /** Profile id selected by chat capability hooks (value-mapping). */
    fun profileId(): String = chat.valueMapping.embedding

    /** Active embedding data profile or fails fast when undefined. */
    fun profile(): DataEmbeddingConfigurationProperties.EmbeddingDataProfile {
        val id = profileId()
        return data.embedding[id]
            ?: error("mill.ai.data.embedding['$id'] is not defined (chat.value-mapping.embedding='$id')")
    }

    /** v1 constraints: at most one source; only `metadata-facets` is implemented. */
    fun validateProfile() {
        val sources = profile().sources
        require(sources.size <= 1) {
            "mill.ai.data.embedding['${profileId()}'].sources supports at most one entry in v1 (found ${sources.size})"
        }
        sources.forEach { source ->
            val type = source.type?.trim().orEmpty()
            require(type.equals("metadata-facets", ignoreCase = true)) {
                "Unsupported embedding source type '$type' on profile '${profileId()}'; v1 supports metadata-facets only"
            }
        }
    }
}
