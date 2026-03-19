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

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

/**
 * In-memory [ChatMemoryStore] backed by a Caffeine cache.
 *
 * Bounded by [maxConversations] and a per-entry TTL ([ttl]) measured from last access.
 * Entries are silently evicted once either limit is exceeded, preventing unbounded
 * memory growth in long-running processes.
 *
 * Defaults are conservative for interactive/dev use:
 * - 1 000 concurrent conversations
 * - 30-minute idle TTL
 *
 * Durable adapters in `mill-persistence` will supersede this for production use.
 */
class InMemoryChatMemoryStore(
    val maxConversations: Long = 1_000,
    val ttl: Duration = Duration.ofMinutes(30),
) : ChatMemoryStore {

    private val cache = Caffeine.newBuilder()
        .maximumSize(maxConversations)
        .expireAfterAccess(ttl)
        .build<String, ConversationMemory>()

    override fun load(conversationId: String): ConversationMemory? =
        cache.getIfPresent(conversationId)

    override fun save(memory: ConversationMemory) {
        cache.put(memory.conversationId, memory)
    }

    override fun clear(conversationId: String) {
        cache.invalidate(conversationId)
    }
}





