package io.qpointz.mill.ai.langchain4j

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonlLineBufferTest {

    private fun buffer(lines: MutableList<String> = mutableListOf()): Pair<JsonlLineBuffer, MutableList<String>> {
        val buf = JsonlLineBuffer(onLine = { lines.add(it) })
        return buf to lines
    }

    // ── line emission on newline ───────────────────────────────────────────────

    @Test
    fun `should emit line when newline arrives in same chunk`() {
        val (buf, lines) = buffer()
        buf.onToken("hello\n")
        assertEquals(listOf("hello"), lines)
    }

    @Test
    fun `should emit line when newline splits across two chunks`() {
        val (buf, lines) = buffer()
        buf.onToken("hel")
        buf.onToken("lo\n")
        assertEquals(listOf("hello"), lines)
    }

    @Test
    fun `should emit multiple lines from single chunk with multiple newlines`() {
        val (buf, lines) = buffer()
        buf.onToken("a\nb\nc\n")
        assertEquals(listOf("a", "b", "c"), lines)
    }

    @Test
    fun `should emit multiple lines from chunks with mixed delimiters`() {
        val (buf, lines) = buffer()
        buf.onToken("{\"event\":\"begin\"}\n{\"ev")
        buf.onToken("ent\":\"end\"}\n")
        assertEquals(listOf("{\"event\":\"begin\"}", "{\"event\":\"end\"}"), lines)
    }

    // ── empty-line skipping ───────────────────────────────────────────────────

    @Test
    fun `should skip empty lines`() {
        val (buf, lines) = buffer()
        buf.onToken("\n\nhello\n\n")
        assertEquals(listOf("hello"), lines)
    }

    @Test
    fun `should skip newline-only chunk`() {
        val (buf, lines) = buffer()
        buf.onToken("\n")
        assertTrue(lines.isEmpty())
    }

    // ── complete flush ────────────────────────────────────────────────────────

    @Test
    fun `should flush remaining content without trailing newline on complete`() {
        val (buf, lines) = buffer()
        buf.onToken("last line")
        buf.complete()
        assertEquals(listOf("last line"), lines)
    }

    @Test
    fun `should not emit empty string on complete when buffer is empty`() {
        val (buf, lines) = buffer()
        buf.complete()
        assertTrue(lines.isEmpty())
    }

    @Test
    fun `should emit all lines then flush remainder on complete`() {
        val (buf, lines) = buffer()
        buf.onToken("first\nsecond")
        buf.complete()
        assertEquals(listOf("first", "second"), lines)
    }

    // ── complete callback ─────────────────────────────────────────────────────

    @Test
    fun `should invoke onComplete callback`() {
        var completed = false
        val buf = JsonlLineBuffer(onLine = {}, onComplete = { completed = true })
        buf.complete()
        assertTrue(completed)
    }

    // ── error propagation ─────────────────────────────────────────────────────

    @Test
    fun `should propagate error to onError callback`() {
        var caught: Throwable? = null
        val buf = JsonlLineBuffer(onLine = {}, onError = { caught = it })
        val ex = RuntimeException("stream failure")
        buf.error(ex)
        assertEquals(ex, caught)
    }

    // ── null chunk handling ───────────────────────────────────────────────────

    @Test
    fun `should handle null token without emission`() {
        val (buf, lines) = buffer()
        buf.onToken(null)
        buf.complete()
        assertTrue(lines.isEmpty())
    }
}
