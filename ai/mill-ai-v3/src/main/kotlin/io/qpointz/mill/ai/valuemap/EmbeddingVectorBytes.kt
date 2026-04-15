package io.qpointz.mill.ai.valuemap

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * IEEE-754 little-endian encoding of [FloatArray] for persistence in `VARBINARY` columns.
 */
object EmbeddingVectorBytes {

    /**
     * Encodes [vector] to raw bytes (little-endian floats).
     */
    fun encode(vector: FloatArray): ByteArray {
        val bb = ByteBuffer.allocate(vector.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in vector) {
            bb.putFloat(f)
        }
        return bb.array()
    }

    /**
     * Decodes bytes produced by [encode], or returns `null` if [bytes] is null or empty.
     */
    fun decode(bytes: ByteArray?): FloatArray? {
        if (bytes == null || bytes.isEmpty()) {
            return null
        }
        require(bytes.size % 4 == 0) { "embedding byte length must be multiple of 4" }
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val n = bytes.size / 4
        return FloatArray(n) { bb.getFloat() }
    }
}
