package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.persistence.ChatUpdate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class InMemoryChatRegistryTest {

    private lateinit var registry: InMemoryChatRegistry

    @BeforeEach
    fun setUp() {
        registry = InMemoryChatRegistry()
    }

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
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @Test
    fun shouldCreateAndLoad() {
        val c = registry.create(chat(chatId = "c1"))
        assertEquals("c1", registry.load("c1")?.chatId)
    }

    @Test
    fun shouldReturnNull_whenLoadingMissingChat() {
        assertNull(registry.load("missing"))
    }

    @Test
    fun shouldListChats_forUser() {
        registry.create(chat(userId = "user-1"))
        registry.create(chat(userId = "user-1"))
        registry.create(chat(userId = "user-2"))
        assertEquals(2, registry.list("user-1").size)
    }

    @Test
    fun shouldReturnEmpty_whenUserHasNoChats() {
        assertTrue(registry.list("nobody").isEmpty())
    }

    @Test
    fun shouldListChats_sortedByCreatedAtDescending() {
        val older = chat(chatId = "old").copy(createdAt = Instant.now().minusSeconds(60))
        val newer = chat(chatId = "new").copy(createdAt = Instant.now())
        registry.create(older)
        registry.create(newer)
        val list = registry.list("user-1")
        assertEquals("new", list[0].chatId)
        assertEquals("old", list[1].chatId)
    }

    @Test
    fun shouldUpdateChatName() {
        registry.create(chat(chatId = "c1", chatName = "Old Name"))
        registry.update("c1", ChatUpdate(chatName = "New Name"))
        assertEquals("New Name", registry.load("c1")?.chatName)
    }

    @Test
    fun shouldUpdateFavoriteFlag() {
        registry.create(chat(chatId = "c1", isFavorite = false))
        registry.update("c1", ChatUpdate(isFavorite = true))
        assertTrue(registry.load("c1")?.isFavorite == true)
    }

    @Test
    fun shouldReturnNull_whenUpdatingMissingChat() {
        assertNull(registry.update("missing", ChatUpdate(chatName = "x")))
    }

    @Test
    fun shouldRefreshUpdatedAt_onUpdate() {
        val before = Instant.now().minusSeconds(10)
        registry.create(chat(chatId = "c1").copy(updatedAt = before))
        registry.update("c1", ChatUpdate(chatName = "Changed"))
        assertTrue(registry.load("c1")!!.updatedAt.isAfter(before))
    }

    @Test
    fun shouldNotMutateImmutableFields_onUpdate() {
        registry.create(chat(chatId = "c1", userId = "user-1", profileId = "profile-a"))
        registry.update("c1", ChatUpdate(chatName = "Changed"))
        val loaded = registry.load("c1")!!
        assertEquals("user-1", loaded.userId)
        assertEquals("profile-a", loaded.profileId)
        assertEquals("c1", loaded.chatId)
    }

    @Test
    fun shouldDeleteChat() {
        registry.create(chat(chatId = "c1"))
        assertTrue(registry.delete("c1"))
        assertNull(registry.load("c1"))
    }

    @Test
    fun shouldReturnFalse_whenDeletingMissingChat() {
        assertFalse(registry.delete("missing"))
    }

    @Test
    fun shouldFindByContext() {
        registry.create(chat(chatId = "c1", contextType = "model", contextId = "sales.customers"))
        val found = registry.findByContext("user-1", "model", "sales.customers")
        assertEquals("c1", found?.chatId)
    }

    @Test
    fun shouldReturnNull_whenContextNotFound() {
        assertNull(registry.findByContext("user-1", "model", "no.such.context"))
    }

    @Test
    fun shouldIsolateContextByUser() {
        registry.create(chat(chatId = "c1", userId = "user-1", contextType = "model", contextId = "sales"))
        registry.create(chat(chatId = "c2", userId = "user-2", contextType = "model", contextId = "sales"))
        assertEquals("c1", registry.findByContext("user-1", "model", "sales")?.chatId)
        assertEquals("c2", registry.findByContext("user-2", "model", "sales")?.chatId)
    }
}
