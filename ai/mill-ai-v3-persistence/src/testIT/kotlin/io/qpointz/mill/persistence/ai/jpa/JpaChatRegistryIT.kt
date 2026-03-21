package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatRegistry
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMetadataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaChatRegistryIT {

    @Autowired lateinit var repo: ChatMetadataRepository

    private val registry by lazy { JpaChatRegistry(repo) }

    private fun chat(
        chatId: String = UUID.randomUUID().toString(),
        userId: String = "user-1",
        profileId: String = "profile-a",
        chatName: String = "Test Chat",
        chatType: String = "general",
        isFavorite: Boolean = false,
        contextType: String? = null,
        contextId: String? = null,
        contextLabel: String? = null,
        contextEntityType: String? = null,
    ) = ChatMetadata(
        chatId = chatId,
        userId = userId,
        profileId = profileId,
        chatName = chatName,
        chatType = chatType,
        isFavorite = isFavorite,
        contextType = contextType,
        contextId = contextId,
        contextLabel = contextLabel,
        contextEntityType = contextEntityType,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @Test
    fun `create and load roundtrip`() {
        val c = registry.create(chat(chatId = "c1"))
        val loaded = registry.load("c1")
        assertThat(loaded).isNotNull
        assertThat(loaded!!.chatId).isEqualTo("c1")
        assertThat(loaded.profileId).isEqualTo("profile-a")
    }

    @Test
    fun `load returns null for missing chat`() {
        assertThat(registry.load("missing")).isNull()
    }

    @Test
    fun `list returns chats for user ordered by createdAt desc`() {
        val older = chat(userId = "user-1").copy(createdAt = Instant.now().minusSeconds(60))
        val newer = chat(userId = "user-1").copy(createdAt = Instant.now())
        registry.create(older)
        registry.create(newer)
        registry.create(chat(userId = "user-2"))

        val list = registry.list("user-1")
        assertThat(list).hasSize(2)
        assertThat(list[0].createdAt).isAfterOrEqualTo(list[1].createdAt)
    }

    @Test
    fun `update persists chatName change`() {
        registry.create(chat(chatId = "c2", chatName = "Old"))
        registry.update("c2", ChatUpdate(chatName = "New"))
        assertThat(registry.load("c2")?.chatName).isEqualTo("New")
    }

    @Test
    fun `update persists isFavorite change`() {
        registry.create(chat(chatId = "c3", isFavorite = false))
        registry.update("c3", ChatUpdate(isFavorite = true))
        assertThat(registry.load("c3")?.isFavorite).isTrue()
    }

    @Test
    fun `update returns null for missing chat`() {
        assertThat(registry.update("missing", ChatUpdate(chatName = "x"))).isNull()
    }

    @Test
    fun `update does not mutate immutable fields`() {
        registry.create(chat(chatId = "c9", userId = "user-1", profileId = "profile-a"))
        registry.update("c9", ChatUpdate(chatName = "Changed"))
        val loaded = registry.load("c9")!!
        assertThat(loaded.userId).isEqualTo("user-1")
        assertThat(loaded.profileId).isEqualTo("profile-a")
        assertThat(loaded.chatId).isEqualTo("c9")
    }

    @Test
    fun `delete removes chat and returns true`() {
        registry.create(chat(chatId = "c4"))
        assertThat(registry.delete("c4")).isTrue()
        assertThat(registry.load("c4")).isNull()
    }

    @Test
    fun `delete returns false for missing chat`() {
        assertThat(registry.delete("missing")).isFalse()
    }

    @Test
    fun `findByContext returns matching chat`() {
        registry.create(chat(chatId = "c5", contextType = "model", contextId = "sales.customers"))
        val found = registry.findByContext("user-1", "model", "sales.customers")
        assertThat(found?.chatId).isEqualTo("c5")
    }

    @Test
    fun `findByContext returns null when no match`() {
        assertThat(registry.findByContext("user-1", "model", "no.such")).isNull()
    }

    @Test
    fun `findByContext isolates by userId`() {
        registry.create(chat(chatId = "c6", userId = "user-1", contextType = "model", contextId = "sales"))
        registry.create(chat(chatId = "c7", userId = "user-2", contextType = "model", contextId = "sales"))
        assertThat(registry.findByContext("user-1", "model", "sales")?.chatId).isEqualTo("c6")
        assertThat(registry.findByContext("user-2", "model", "sales")?.chatId).isEqualTo("c7")
    }

    @Test
    fun `all ChatMetadata fields survive roundtrip`() {
        val original = chat(
            chatId = "c8",
            chatName = "My Chat",
            chatType = "contextual",
            isFavorite = true,
            contextType = "model",
            contextId = "orders",
            contextLabel = "Orders",
            contextEntityType = "table",
        )
        registry.create(original)
        val loaded = registry.load("c8")!!
        assertThat(loaded.chatName).isEqualTo("My Chat")
        assertThat(loaded.chatType).isEqualTo("contextual")
        assertThat(loaded.isFavorite).isTrue()
        assertThat(loaded.contextType).isEqualTo("model")
        assertThat(loaded.contextId).isEqualTo("orders")
        assertThat(loaded.contextLabel).isEqualTo("Orders")
        assertThat(loaded.contextEntityType).isEqualTo("table")
    }
}
