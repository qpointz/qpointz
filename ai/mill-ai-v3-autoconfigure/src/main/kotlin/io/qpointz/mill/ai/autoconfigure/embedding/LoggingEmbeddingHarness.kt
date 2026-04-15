package io.qpointz.mill.ai.autoconfigure.embedding

import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.embedding.EmbeddingModelPersistenceDescriptor
import org.slf4j.LoggerFactory

/**
 * Adds debug-level operation logging without printing vectors or secrets.
 */
class LoggingEmbeddingHarness(
    private val delegate: EmbeddingHarness,
) : EmbeddingHarness {

    private val log = LoggerFactory.getLogger(javaClass)

    override val dimension: Int get() = delegate.dimension

    override val persistence: EmbeddingModelPersistenceDescriptor
        get() = delegate.persistence

    override fun embed(text: String): FloatArray {
        log.debug("Embedding request textLength={} dimension={}", text.length, dimension)
        return delegate.embed(text)
    }
}
