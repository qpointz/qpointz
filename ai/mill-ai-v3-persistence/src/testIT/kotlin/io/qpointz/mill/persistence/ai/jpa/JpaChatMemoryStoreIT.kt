package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.memory.ConversationMemory
import io.qpointz.mill.ai.runtime.ConversationMessage
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryMessageRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryRepository
import io.qpointz.mill.ai.runtime.MessageRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaChatMemoryStoreIT {

    @Autowired lateinit var memoryRepo: ChatMemoryRepository
    @Autowired lateinit var messageRepo: ChatMemoryMessageRepository

    private val store by lazy { JpaChatMemoryStore(memoryRepo, messageRepo) }

    @Test
    fun `should return null for unknown conversation`() {
        assertThat(store.load("unknown")).isNull()
    }

    @Test
    fun `should save and load conversation memory`() {
        val memory = ConversationMemory(
            conversationId = "conv-1",
            profileId = "test-profile",
            messages = listOf(
                ConversationMessage(MessageRole.USER, "hello"),
                ConversationMessage(MessageRole.ASSISTANT, "hi there"),
            )
        )
        store.save(memory)
        val loaded = store.load("conv-1")
        assertThat(loaded).isNotNull
        assertThat(loaded!!.messages).hasSize(2)
        assertThat(loaded.messages[0].role).isEqualTo(MessageRole.USER)
        assertThat(loaded.messages[1].content).isEqualTo("hi there")
    }

    @Test
    fun `save replaces messages transactionally`() {
        val initial = ConversationMemory("conv-2", "p", listOf(
            ConversationMessage(MessageRole.USER, "first"),
        ))
        store.save(initial)
        val updated = ConversationMemory("conv-2", "p", listOf(
            ConversationMessage(MessageRole.USER, "replaced"),
        ))
        store.save(updated)
        val loaded = store.load("conv-2")!!
        assertThat(loaded.messages).hasSize(1)
        assertThat(loaded.messages[0].content).isEqualTo("replaced")
    }

    @Test
    fun `clear removes the conversation`() {
        store.save(ConversationMemory("conv-3", "p", listOf(
            ConversationMessage(MessageRole.USER, "msg"),
        )))
        store.clear("conv-3")
        assertThat(store.load("conv-3")).isNull()
    }
}
