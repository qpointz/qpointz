package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.persistence.ChatMetadata
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class ChatRehydrationTest {

    private val registry = MapProfileRegistry(
        HelloWorldAgentProfile.profile,
        SchemaExplorationAgentProfile.profile,
    )

    private fun metadata(
        profileId: String = "hello-world",
        contextType: String? = null,
        contextId: String? = null,
        contextEntityType: String? = null,
    ) = ChatMetadata(
        chatId = "chat-1",
        userId = "user-1",
        profileId = profileId,
        chatName = "Test Chat",
        chatType = if (contextType == null) "general" else "contextual",
        contextType = contextType,
        contextId = contextId,
        contextEntityType = contextEntityType,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @Test
    fun shouldResolveProfile_forKnownProfileId() {
        val result = registry.rehydrate(metadata(profileId = "hello-world"))
        assertNotNull(result)
        assertEquals("hello-world", result!!.profile.id)
    }

    @Test
    fun shouldReturnNull_forUnknownProfileId() {
        assertNull(registry.rehydrate(metadata(profileId = "no-such-profile")))
    }

    @Test
    fun shouldDefaultContextType_toGeneral_whenNullInMetadata() {
        val result = registry.rehydrate(metadata(contextType = null))!!
        assertEquals("general", result.agentContext.contextType)
    }

    @Test
    fun shouldPreserveContextType_whenPresentInMetadata() {
        val result = registry.rehydrate(metadata(contextType = "model"))!!
        assertEquals("model", result.agentContext.contextType)
    }

    @Test
    fun shouldMapContextId_toFocusEntityId() {
        val result = registry.rehydrate(metadata(contextId = "sales.customers"))!!
        assertEquals("sales.customers", result.agentContext.focusEntityId)
    }

    @Test
    fun shouldMapContextEntityType_toFocusEntityType() {
        val result = registry.rehydrate(metadata(contextEntityType = "table"))!!
        assertEquals("table", result.agentContext.focusEntityType)
    }

    @Test
    fun shouldPreserveMetadata_inRehydrationContext() {
        val meta = metadata(profileId = "hello-world", contextType = "model", contextId = "orders")
        val result = registry.rehydrate(meta)!!
        assertEquals(meta, result.metadata)
    }

    @Test
    fun shouldHaveNullFocusFields_forGeneralChat() {
        val result = registry.rehydrate(metadata())!!
        assertNull(result.agentContext.focusEntityType)
        assertNull(result.agentContext.focusEntityId)
    }
}
