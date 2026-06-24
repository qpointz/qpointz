package io.qpointz.mill.data.odata.read

import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.proto.VectorBlockSchema
import io.qpointz.mill.vectors.VectorBlockIterator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ODataEntityReaderTest {

    @Test
    fun shouldReturnEmptyList_whenVectorBlockHasZeroRows() {
        val emptyBlock = VectorBlock.newBuilder().setVectorSize(0).build()
        val reader = ODataEntityReader()

        val rows = reader.readAll(iteratorOf(emptyBlock))

        assertThat(rows).isEmpty()
    }

    @Test
    fun shouldReturnEmptyList_whenIteratorHasNoBlocks() {
        val reader = ODataEntityReader()

        val rows = reader.readAll(iteratorOf())

        assertThat(rows).isEmpty()
    }

    private fun iteratorOf(vararg blocks: VectorBlock): VectorBlockIterator =
        object : VectorBlockIterator {
            private val delegate = blocks.toList().iterator()

            override fun hasNext(): Boolean = delegate.hasNext()

            override fun next(): VectorBlock = delegate.next()

            override fun remove() {
                throw UnsupportedOperationException()
            }

            override fun schema(): VectorBlockSchema? = blocks.firstOrNull()?.schema
        }
}
