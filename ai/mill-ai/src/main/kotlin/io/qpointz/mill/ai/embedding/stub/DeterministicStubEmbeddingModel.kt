package io.qpointz.mill.ai.embedding.stub

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.output.Response
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.math.sqrt

/**
 * Deterministic [EmbeddingModel] (SHA-256 expansion + L2 normalize) for tests and offline use.
 */
class DeterministicStubEmbeddingModel(
    private val dimension: Int,
) : EmbeddingModel {

    override fun embed(text: String): Response<Embedding> {
        val e = Embedding.from(vectorForText(text))
        e.normalize()
        return Response.from(e)
    }

    override fun embed(segment: TextSegment): Response<Embedding> = embed(segment.text())

    override fun embedAll(segments: List<TextSegment>): Response<List<Embedding>> {
        val list = segments.map { embed(it.text()).content() }
        return Response.from(list)
    }

    override fun dimension(): Int = dimension

    private fun vectorForText(text: String): FloatArray {
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(StandardCharsets.UTF_8))
        val v = FloatArray(dimension)
        var i = 0
        while (i < dimension) {
            val b0 = digest[i % digest.size].toInt() and 0xFF
            val b1 = digest[(i + 7) % digest.size].toInt() and 0xFF
            v[i] = (b0 / 255.0f) * 2.0f - 1.0f + (b1 / 255.0f) * 0.01f
            i++
        }
        var sumSq = 0.0
        for (j in v.indices) {
            sumSq += (v[j] * v[j]).toDouble()
        }
        val norm = sqrt(sumSq).toFloat().coerceAtLeast(1e-6f)
        for (j in v.indices) {
            v[j] /= norm
        }
        return v
    }
}
