package io.qpointz.mill.ai.embedding

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel

/**
 * [EmbeddingHarness] backed by a LangChain4j [EmbeddingModel].
 *
 * Persistence identity uses [persistence]; the fingerprint format is defined by [companion.describeForEmbeddingProfile].
 */
class LangChain4jEmbeddingHarness(
    private val model: EmbeddingModel,
    override val persistence: EmbeddingModelPersistenceDescriptor,
) : EmbeddingHarness {

    override val dimension: Int get() = persistence.dimension

    override fun embed(text: String): FloatArray {
        val response = model.embed(TextSegment.from(text))
        return response.content().vector()
    }

    companion object {

        /**
         * Builds persistence metadata for a named embedding profile (`mill.ai.embedding-model.<key>`).
         * Fingerprint: `provider|dimension|modelName|profileKey` (stable for a given profile and provider settings).
         */
        fun describeForEmbeddingProfile(
            profileKey: String,
            provider: String,
            modelName: String,
            dimension: Int,
            paramsJson: String? = null,
            label: String? = null,
        ): EmbeddingModelPersistenceDescriptor {
            val fingerprint = "$provider|$dimension|$modelName|$profileKey"
            return EmbeddingModelPersistenceDescriptor(
                configFingerprint = fingerprint,
                provider = provider,
                modelId = modelName,
                dimension = dimension,
                paramsJson = paramsJson,
                label = label,
            )
        }
    }
}
