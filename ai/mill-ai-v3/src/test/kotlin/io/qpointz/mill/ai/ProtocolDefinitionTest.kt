package io.qpointz.mill.ai

import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProtocolDefinitionTest {

    private fun emptyObjectSchema() = JsonObjectSchema.builder().build()

    // ── TEXT mode ─────────────────────────────────────────────────────────────

    @Test
    fun `should create valid TEXT protocol`() {
        val p = ProtocolDefinition(
            id = "conv.text",
            description = "Conversational text protocol.",
            mode = ProtocolMode.TEXT,
        )
        assertEquals(ProtocolMode.TEXT, p.mode)
    }

    @Test
    fun `should reject TEXT protocol with finalSchema`() {
        assertThrows<IllegalArgumentException> {
            ProtocolDefinition(
                id = "conv.text",
                description = "Bad.",
                mode = ProtocolMode.TEXT,
                finalSchema = emptyObjectSchema(),
            )
        }
    }

    @Test
    fun `should reject TEXT protocol with events`() {
        assertThrows<IllegalArgumentException> {
            ProtocolDefinition(
                id = "conv.text",
                description = "Bad.",
                mode = ProtocolMode.TEXT,
                events = listOf(
                    ProtocolEventDefinition("row", "A row.", emptyObjectSchema()),
                ),
            )
        }
    }

    // ── STRUCTURED_FINAL mode ─────────────────────────────────────────────────

    @Test
    fun `should create valid STRUCTURED_FINAL protocol`() {
        val p = ProtocolDefinition(
            id = "chart.final",
            description = "Chart spec protocol.",
            mode = ProtocolMode.STRUCTURED_FINAL,
            finalSchema = JsonObjectSchema.builder()
                .description("Chart spec.")
                .addStringProperty("title", "Chart title.")
                .build(),
        )
        assertEquals(ProtocolMode.STRUCTURED_FINAL, p.mode)
    }

    @Test
    fun `should reject STRUCTURED_FINAL protocol without finalSchema`() {
        assertThrows<IllegalArgumentException> {
            ProtocolDefinition(
                id = "chart.final",
                description = "Bad.",
                mode = ProtocolMode.STRUCTURED_FINAL,
            )
        }
    }

    // ── STRUCTURED_STREAM mode ────────────────────────────────────────────────

    @Test
    fun `should create valid STRUCTURED_STREAM protocol`() {
        val p = ProtocolDefinition(
            id = "table.stream",
            description = "Streamed table rows.",
            mode = ProtocolMode.STRUCTURED_STREAM,
            events = listOf(
                ProtocolEventDefinition("row", "A table row.", emptyObjectSchema()),
            ),
        )
        assertEquals(ProtocolMode.STRUCTURED_STREAM, p.mode)
    }

    @Test
    fun `should reject STRUCTURED_STREAM protocol without events`() {
        assertThrows<IllegalArgumentException> {
            ProtocolDefinition(
                id = "table.stream",
                description = "Bad.",
                mode = ProtocolMode.STRUCTURED_STREAM,
            )
        }
    }

    // ── fallbackMode ──────────────────────────────────────────────────────────

    @Test
    fun `should allow STRUCTURED_STREAM to declare TEXT fallback`() {
        val p = ProtocolDefinition(
            id = "table.stream",
            description = "With fallback.",
            mode = ProtocolMode.STRUCTURED_STREAM,
            fallbackMode = ProtocolMode.TEXT,
            events = listOf(ProtocolEventDefinition("row", "Row.", emptyObjectSchema())),
        )
        assertEquals(ProtocolMode.TEXT, p.fallbackMode)
    }

    @Test
    fun `should allow STRUCTURED_FINAL to declare TEXT fallback`() {
        val p = ProtocolDefinition(
            id = "chart.final",
            description = "With fallback.",
            mode = ProtocolMode.STRUCTURED_FINAL,
            fallbackMode = ProtocolMode.TEXT,
            finalSchema = emptyObjectSchema(),
        )
        assertEquals(ProtocolMode.TEXT, p.fallbackMode)
    }

    @Test
    fun `should allow no fallback on TEXT protocol`() {
        val p = ProtocolDefinition(
            id = "conv.text",
            description = "No fallback needed.",
            mode = ProtocolMode.TEXT,
        )
        assertEquals(null, p.fallbackMode)
    }
}
