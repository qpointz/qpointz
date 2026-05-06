package io.qpointz.mill.source.format.json.export

import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.proto.VectorBlockSchema
import io.qpointz.mill.vectors.VectorBlockIterator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class JsonArrayStreamingEncoderTest {

    private class EmptyBlocks : VectorBlockIterator {
        override fun schema(): VectorBlockSchema = VectorBlockSchema.getDefaultInstance()
        override fun hasNext(): Boolean = false
        override fun next(): VectorBlock = throw NoSuchElementException()
        override fun remove() {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun `empty iterator yields empty json array`() {
        val out = ByteArrayOutputStream()
        JsonArrayStreamingEncoder.writeJsonArray(EmptyBlocks(), out)
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("[]")
    }
}
