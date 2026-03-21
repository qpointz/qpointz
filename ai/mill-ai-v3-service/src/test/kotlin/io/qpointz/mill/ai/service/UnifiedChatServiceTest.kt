package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.autoconfigure.chat.AiV3ChatRuntime
import io.qpointz.mill.ai.autoconfigure.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.autoconfigure.chat.MillAiV3ChatProperties
import io.qpointz.mill.ai.memory.InMemoryChatMemoryStore
import io.qpointz.mill.ai.persistence.InMemoryChatRegistry
import io.qpointz.mill.ai.persistence.InMemoryConversationStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux

class UnifiedChatServiceTest {

    private val registry = InMemoryChatRegistry()
    private val conversationStore = InMemoryConversationStore()
    private val chatMemoryStore = InMemoryChatMemoryStore()
    private val runtime = AiV3ChatRuntime { _, message ->
        Flux.just(
            ChatRuntimeEvent.Chunk("echo:$message"),
            ChatRuntimeEvent.Completed("echo:$message"),
        )
    }
    private val service = UnifiedChatService(
        registry = registry,
        conversationStore = conversationStore,
        chatMemoryStore = chatMemoryStore,
        runtime = runtime,
        properties = MillAiV3ChatProperties(),
    )

    @Test
    fun `should create general chats and list them`() {
        val result = service.createChat(null)

        assertThat(result.created).isTrue()
        assertThat(result.chat.chatName).isEqualTo("New Chat")
        assertThat(service.listChats()).extracting("chatId").containsExactly(result.chat.chatId)
    }

    @Test
    fun `should exclude contextual chats from listChats`() {
        service.createChat(null)
        service.createChat(CreateChatRequest(contextType = "model", contextId = "x", contextLabel = "X"))

        val listed = service.listChats()
        assertThat(listed).allMatch { it.chatType == "general" }
    }

    @Test
    fun `should reuse contextual chat for same context`() {
        val first = service.createChat(CreateChatRequest(contextType = "model", contextId = "sales.customers", contextLabel = "customers"))
        val second = service.createChat(CreateChatRequest(contextType = "model", contextId = "sales.customers", contextLabel = "customers"))

        assertThat(first.created).isTrue()
        assertThat(second.created).isFalse()
        assertThat(second.chat.chatId).isEqualTo(first.chat.chatId)
        assertThat(service.getChatByContext("model", "sales.customers")?.chatId).isEqualTo(first.chat.chatId)
    }

    @Test
    fun `should derive general chat title from first user message`() {
        val chat = service.createChat(null).chat

        val events = service.sendMessage(chat.chatId, "How do I expose a unified chat API?")
            .collectList()
            .block()!!

        assertThat(events).hasSize(2)
        assertThat(service.getChat(chat.chatId)?.chat?.chatName).isEqualTo("How do I expose a unified chat...")
    }

    @Test
    fun `should delete chats from registry`() {
        val chat = service.createChat(null).chat

        assertThat(service.deleteChat(chat.chatId)).isTrue()
        assertThat(service.getChat(chat.chatId)).isNull()
    }
}
