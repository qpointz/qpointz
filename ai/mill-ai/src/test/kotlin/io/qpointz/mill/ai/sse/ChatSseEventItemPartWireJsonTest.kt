package io.qpointz.mill.ai.sse

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Guards the mill-ui contract: structured [ChatSseEvent.ItemPartUpdated] rows must carry
 * explicit `presentation` / `partType` on the wire so browsers do not fall back to V1
 * (`conversation` + `text`) and append JSON into the main bubble.
 */
class ChatSseEventItemPartWireJsonTest {

    private val mapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .build()

    @Test
    fun shouldIncludePresentationAndPartType_onStructuredPartUpdate() {
        val now = java.time.Instant.parse("2026-01-01T00:00:00Z")
        val e = ChatSseEvent.ItemPartUpdated(
            eventId = "e1",
            chatId = "c1",
            itemId = "i1",
            sequence = 0,
            timestamp = now,
            presentation = "structured",
            partType = "facet-proposal",
            mode = "replace",
            content = """{"facetTypeKey":"k","metadataEntityId":"e"}""",
        )
        val json = mapper.writeValueAsString(e)
        assertTrue(json.contains("\"presentation\":\"structured\""), json)
        assertTrue(json.contains("\"partType\":\"facet-proposal\""), json)
    }

    @Test
    fun shouldIncludePresentationAndPartType_onV1Defaults() {
        val now = java.time.Instant.parse("2026-01-01T00:00:00Z")
        val e = ChatSseEvent.ItemPartUpdated(
            eventId = "e1",
            chatId = "c1",
            itemId = "i1",
            sequence = 0,
            timestamp = now,
            content = "hello",
        )
        val json = mapper.writeValueAsString(e)
        assertTrue(json.contains("\"presentation\":\"conversation\""), json)
        assertTrue(json.contains("\"partType\":\"text\""), json)
    }

    @Test
    fun shouldRoundTripStructuredPartDiscriminators() {
        val now = java.time.Instant.parse("2026-01-01T00:00:00Z")
        val original = ChatSseEvent.ItemPartUpdated(
            eventId = "e1",
            chatId = "c1",
            itemId = "i1",
            sequence = 1,
            timestamp = now,
            presentation = "structured",
            partType = "sql",
            mode = "replace",
            content = """{"sql":"SELECT 1"}""",
        )
        val json = mapper.writeValueAsString(original)
        val tree = mapper.readTree(json)
        assertNotNull(tree.get("presentation"))
        assertNotNull(tree.get("partType"))
        assertTrue(tree.get("presentation").asText() == "structured")
        assertTrue(tree.get("partType").asText() == "sql")
    }
}
