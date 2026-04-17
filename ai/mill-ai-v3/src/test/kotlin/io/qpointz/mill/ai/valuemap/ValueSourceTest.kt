package io.qpointz.mill.ai.valuemap

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValueSourceTest {

    @Test
    fun `composite concatenates in order`() {
        val a = StaticListValueSource("a" to "A")
        val b = StaticListValueSource("b" to "B")
        val c = CompositeValueSource(listOf(a, b))
        assertThat(c.provideEntries().map { it.content }).containsExactly("a", "b")
    }

    @Test
    fun `distinct column builds prefixed content and value metadata`() {
        val s = DistinctColumnValueSource("ctx:", listOf("x", "y"))
        val e = s.provideEntries()
        assertThat(e[0].content).isEqualTo("ctx:x")
        assertThat(e[0].metadata["value"]).isEqualTo("x")
    }
}
