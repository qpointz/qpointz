package io.qpointz.mill.ai.memory

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InMemoryChatMemoryStoreTest {

    private fun store() = InMemoryChatMemoryStore()

    private fun memory(id: String, vararg messages: ConversationMessage) =
        ConversationMemory(conversationId = id, profileId = "test", messages = messages.toList())

    @Test
    fun `shouldReturnNull_whenConversationNotFound`() {
        assertNull(store().load("unknown"))
    }

    @Test
    fun `shouldPersistAndLoad_whenSaved`() {
        val s = store()
        val m = memory("c1", ConversationMessage(MessageRole.USER, "hello"))
        s.save(m)
        assertEquals(m, s.load("c1"))
    }

    @Test
    fun `shouldOverwrite_whenSavedTwice`() {
        val s = store()
        s.save(memory("c1", ConversationMessage(MessageRole.USER, "first")))
        val updated = memory("c1", ConversationMessage(MessageRole.USER, "second"))
        s.save(updated)
        assertEquals(updated, s.load("c1"))
    }

    @Test
    fun `shouldReturnNull_afterClear`() {
        val s = store()
        s.save(memory("c1", ConversationMessage(MessageRole.USER, "hi")))
        s.clear("c1")
        assertNull(s.load("c1"))
    }

    @Test
    fun `shouldIsolateConversations_byId`() {
        val s = store()
        s.save(memory("c1", ConversationMessage(MessageRole.USER, "conv1")))
        s.save(memory("c2", ConversationMessage(MessageRole.USER, "conv2")))
        s.clear("c1")
        assertNull(s.load("c1"))
        assertNotNull(s.load("c2"))
    }

    @Test
    fun `shouldSilentlyIgnore_clearOfUnknownId`() {
        assertDoesNotThrow { store().clear("nonexistent") }
    }
}





